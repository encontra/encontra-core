package pt.inevo.encontra.query;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.dispatch.CompletableFuture;
import akka.dispatch.Future;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.common.ResultSetDefaultImpl;
import pt.inevo.encontra.index.search.AbstractSearcher;
import pt.inevo.encontra.query.criteria.exps.*;
import pt.inevo.encontra.query.operatorprocessors.SimilarOperatorProcessor;
import pt.inevo.encontra.storage.IEntity;
import scala.Option;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* Default parallel implementation for the query processor.
*
* @author Ricardo
*/
public class QueryProcessorDefaultParallelImpl<E extends IEntity> extends QueryProcessorDefaultImpl<E> {

    protected Class resultClass;
    protected ResultSetOperations combiner;

    public QueryProcessorDefaultParallelImpl() {
        super();
        combiner = new ResultSetOperations();
        queryParser = new QueryParserDefaultImpl();
        logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public void setTopSearcher(AbstractSearcher topSearcher) {
        super.setTopSearcher(topSearcher);
        combiner.setStorage(topSearcher.getObjectStorage());
    }

    /**
     * Actor for applying boolean operators: AND and OR.
     */
    class BooleanSearcherActor extends UntypedActor {

        protected int numAnswers, possibleAnswers;
        protected List results;
        protected ActorRef originalActor;
        protected CompletableFuture future;
        protected QueryParserNode node;
        protected Map<ActorRef, QueryParserNode> runningActors;
        protected int previousResultsCount = -1;
        protected int originalLimit = 0;
        protected int resultPartsCount = 0, previousPartsCount = 0;

        public BooleanSearcherActor() {
            results = new ArrayList<ResultSetDefaultImpl>();
            runningActors = new HashMap<ActorRef, QueryParserNode>();
        }

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof QueryParserNode) {
                if (getContext().getSenderFuture().isDefined()) {
                    future = (CompletableFuture) getContext().getSenderFuture().get();
                } else if (getContext().getSender().isDefined()) {
                    originalActor = (ActorRef) getContext().getSender().get();
                }

                previousResultsCount = -1;
                originalLimit = 0;
                resultPartsCount = 0;
                previousPartsCount = 0;

                node = (QueryParserNode) message;
                originalLimit = node.limit;
                List<QueryParserNode> nodes = node.childrenNodes;

                possibleAnswers = nodes.size();
                for (QueryParserNode n : nodes) {
                    if (n.predicateType.equals(Similar.class)
                            || n.predicateType.equals(Equal.class)
                            || n.predicateType.equals(NotEqual.class)) {

                        ActorRef actorRef = UntypedActor.actorOf(new UntypedActorFactory() {

                            @Override
                            public UntypedActor create() {
                                return new SimilarEqualParallelSearcherActor();
                            }
                        }).start();

                        actorRef.sendOneWay(n, getContext());
                        runningActors.put(actorRef, n);
                    } else if (n.predicateType.equals(And.class)
                            || n.predicateType.equals(Or.class)) {
                        ActorRef andActorRef = UntypedActor.actorOf(new UntypedActorFactory() {

                            @Override
                            public UntypedActor create() {
                                return new BooleanSearcherActor();
                            }
                        }).start();

                        andActorRef.sendOneWay(n, getContext());
                        runningActors.put(andActorRef, n);
                    }
                }
            } else if (message instanceof ResultSet) {

                ResultSet r = (ResultSet) message;
                numAnswers++;
                results.add(r);
                resultPartsCount += r.getSize();

                if (numAnswers >= possibleAnswers) {

                    ResultSet combinedResultSet;

                    if (node.predicateType.equals(And.class)) {
                        combinedResultSet = combiner.intersect(results, originalLimit, node.criteria);
                    } else {
                        combinedResultSet = combiner.join(results, node.distinct, originalLimit, node.criteria);
                    }

                    if (originalActor != null) {
                        originalActor.sendOneWay(combinedResultSet);
                    } else {
                        future.completeWithResult(combinedResultSet);
                    }
                }
            }
        }
    }

    class SimilarEqualParallelSearcherActor extends UntypedActor {

        protected int numAnswers, possibleAnswers;
        protected List partResults;
        protected ResultSet results;
        protected ActorRef originalActor;
        protected CompletableFuture future;
        protected QueryParserNode node;

        public SimilarEqualParallelSearcherActor() {
            partResults = new ArrayList<ResultSetDefaultImpl>();
        }

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof QueryParserNode) {
                if (getContext().getSenderFuture().isDefined()) {
                    future = (CompletableFuture) getContext().getSenderFuture().get();
                } else if (getContext().getSender().isDefined()) {
                    originalActor = (ActorRef) getContext().getSender().get();
                }

                node = (QueryParserNode) message;

                /**
                 * Processes the query and retrieves the results
                 */
                if (node.field != null) {
                    results = new SimilarOperatorProcessor().process(node);
                    getContext().sendOneWay(results, getContext());
                } else {
                    results = new SimilarOperatorProcessor().process(node);
                    getContext().sendOneWay(results, getContext());
                }

            } else if (message instanceof ResultSet) {

                ResultSet r = (ResultSet) message;
                numAnswers++;
                partResults.add(r);

                if (numAnswers >= possibleAnswers) {

                    /**
                     * Do we need to combine the results?
                     */
                    if (partResults.size() > 1) {
                        results = combiner.intersect(partResults, node.limit, node.criteria);
                    } else {
                        results = (ResultSet) partResults.get(0);
                    }

                    /**
                     * Send the results to the actor caller
                     */
                    if (originalActor != null) {
                        originalActor.sendOneWay(results);
                    } else {
                        future.completeWithResult(results);
                    }
                }
            }
        }
    }

    protected ResultSet processAND(QueryParserNode node) {
        ActorRef actor = UntypedActor.actorOf(new UntypedActorFactory() {
            @Override
            public UntypedActor create() {
                return new BooleanSearcherActor();
            }
        }).start();

        return executeQuery(actor, node);
    }

    protected ResultSet processOR(QueryParserNode node) {
        return processAND(node);
    }

    protected ResultSet processSIMILAR(QueryParserNode node, boolean top) {
        ActorRef actor = UntypedActor.actorOf(new UntypedActorFactory() {

            @Override
            public UntypedActor create() {
                return new SimilarEqualParallelSearcherActor();
            }
        }).start();

        ResultSet results = executeQuery(actor, node);
        if (top){
            results = results.getFirstResults(node.limit);
        }
        return results;
    }

    /**
     * Launches the Actor and executes the query, using the QueryParserNode
     *
     * @param actor the actor to start executing
     * @param node  the node to be processed
     * @return
     */
    protected ResultSet executeQuery(ActorRef actor, QueryParserNode node) {
        long begin = Calendar.getInstance().getTimeInMillis();

        //waiting for the processor to output results
        Future future = actor.sendRequestReplyFuture(node, Long.MAX_VALUE, null);
        future.await();

        long end = Calendar.getInstance().getTimeInMillis();

        System.out.println("Process took: " + (end - begin));

        if (future.isCompleted()) {
            Option resultOption = future.result();
            if (resultOption.isDefined()) {
                //everything is ok, so the results were retrieved
                Object result = resultOption.get();
                return (ResultSet) result;
            } else {
                //problem -> something went wrong, must check what happened
                logger.log(Level.SEVERE, "Result wasn't defined. Something went wrong.");
            }
        }

        //something went wrong so return an empty result set
        return new ResultSetDefaultImpl<E>();
    }
}
