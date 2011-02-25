package pt.inevo.encontra.query;

import akka.actor.UntypedActorFactory;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.dispatch.CompletableFuture;
import akka.dispatch.Future;

import java.lang.reflect.Constructor;
import java.util.*;

import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.common.ResultSetDefaultImpl;
import pt.inevo.encontra.engine.QueryProcessor;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.IndexingException;
import pt.inevo.encontra.index.search.Searcher;
import pt.inevo.encontra.query.criteria.CriteriaBuilderImpl;
import pt.inevo.encontra.query.criteria.CriteriaQueryImpl;
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.criteria.exps.And;
import pt.inevo.encontra.query.criteria.exps.Equal;
import pt.inevo.encontra.query.criteria.exps.NotEqual;
import pt.inevo.encontra.query.criteria.exps.Or;
import pt.inevo.encontra.query.criteria.exps.Similar;
import pt.inevo.encontra.storage.IEntity;
import scala.Option;

/**
 * Default parallel implementation for the query processor.
 * @author Ricardo
 */
public class QueryProcessorDefaultParallelImpl<E extends IEntity> extends QueryProcessor<E> {

    protected Class resultClass;
    protected ResultSetOperations combiner;

    public QueryProcessorDefaultParallelImpl() {
        super();
        combiner = new ResultSetOperations();
        queryParser = new QueryParserDefaultImpl();
    }

    /**
     * Actor for applying boolean operators: AND and OR.
     */
    class BooleanSearcherActor extends UntypedActor {

        protected int numAnswers, possibleAnswers;
        protected List results;
        protected ResultSetOperations combiner;
        protected ActorRef originalActor;
        protected CompletableFuture future;
        protected QueryParserNode node;
        protected Map<ActorRef, QueryParserNode> runningActors;
        protected int previousResultsCount = -1;
        protected int originalLimit = 0;
        protected int resultPartsCount = 0, previousPartsCount = 0;

        public BooleanSearcherActor() {
            combiner = new ResultSetOperations();
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
                        combinedResultSet = combiner.intersect(results, originalLimit);
                    } else {
                        combinedResultSet = combiner.join(results, node.distinct, originalLimit);
                    }
                    results.clear();

                    if ((previousResultsCount <= combinedResultSet.getSize() || resultPartsCount > previousPartsCount) && combinedResultSet.getSize() < originalLimit) {
                        for (ActorRef actor : runningActors.keySet()) {
                            QueryParserNode n = runningActors.get(actor);
                            n.limit *= 2;
                            actor.sendOneWay(n, getContext());
                        }

                        previousResultsCount = combinedResultSet.getSize();
                        previousPartsCount = resultPartsCount;
                    } else {
                        if (originalActor != null) {
                            originalActor.sendOneWay(combinedResultSet);
                        } else {
                            future.completeWithResult(combinedResultSet);
                        }
                    }
                }
            }
        }
    }

    class SimilarEqualParallelSearcherActor extends UntypedActor {

        protected int numAnswers, possibleAnswers;
        protected List partResults;
        protected ResultSet results;
        protected ResultSetOperations combiner;
        protected ActorRef originalActor;
        protected CompletableFuture future;
        protected QueryParserNode node;

        public SimilarEqualParallelSearcherActor() {
            combiner = new ResultSetOperations();
            partResults = new ArrayList<ResultSetDefaultImpl>();
        }

        //Creates a sub-query for Equal, Similar and NoEqual, given a node
        private Query createSubQuery(QueryParserNode node, Path path, Object obj) {
            CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
            CriteriaQuery q = cb.createQuery(resultClass);
            try {
                Constructor c = node.predicateType.getConstructor(Expression.class, Object.class);
                CriteriaQuery newQuery = q.where((Expression) c.newInstance(path, obj));
                newQuery = ((CriteriaQueryImpl) newQuery).distinct(node.distinct);
                return newQuery;
            } catch (Exception ex) {
                System.out.println("[Error]: Could not execute the query! Possible reason: " + ex.getMessage());
            }
            return q;
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

                if (node.field != null) {
                    /*
                     * must check the path object - see wheter it is a field from this
                     * class or from other indexed field, in other class
                     */
                    QueryParserNode pathNode = node.childrenNodes.get(0);
                    Path p = (Path) pathNode.predicate;

                    //track the full path of the desired field
                    Queue<Path> relativePaths = new LinkedList<Path>();

                    Path parentPath = p.getParentPath();
                    if (parentPath.isField()) {     //its not a field of this processor
                        while (parentPath.isField()) {
                            relativePaths.add(parentPath);
                            parentPath = parentPath.getParentPath();
                        }

                        if (relativePaths.size() > 0) {
                            parentPath = relativePaths.remove();
                        } else {
                            parentPath = p;
                        }

                        String parentField = parentPath.getAttributeName();
                        Class clazz = parentPath.getJavaType();
                        Path newQueryPath = new Path(clazz);

                        for (Path relPath : relativePaths) {
                            newQueryPath = newQueryPath.get(relPath.getAttributeName());
                        }

                        //in the end we must have the elements we desire
                        newQueryPath = newQueryPath.get(node.field);

                        Searcher searcher = searcherMap.get(parentField);
                        ResultSet resultSet = searcher.search(createSubQuery(node, newQueryPath, node.fieldObject));

                        getContext().sendOneWay(resultSet, getContext());

                    } else {
                        Searcher searcher = searcherMap.get(node.field);
                        ResultSet resultSet = searcher.search(createSubQuery(node, parentPath, node.fieldObject));

                        getContext().sendOneWay(resultSet, getContext());
                    }
                } else {
                    //don't know which searchers to use, so lets dig a bit
                    try {
                        List<IndexedObject> indexedObjects = indexedObjectFactory.processBean((IEntity) node.fieldObject);
                        possibleAnswers = indexedObjects.size();

                        for (IndexedObject obj : indexedObjects) {
                            String fieldName = obj.getName();

                            CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
                            CriteriaQuery query = cb.createQuery(obj.getValue().getClass());
                            Path subModelPath = null;
                            Class clazz = obj.getValue().getClass();

                            //detect if the object is a compound one
                            if (obj.getValue() instanceof IEntity || obj.getValue() instanceof IndexedObject) {
                                clazz = obj.getValue().getClass();
                                subModelPath = query.from(clazz);
                            } else {
                                clazz = node.fieldObject.getClass();
                                subModelPath = query.from(clazz);
                                subModelPath = subModelPath.get(fieldName);
                            }

                            getContext().sendOneWay(createSubQuery(node, subModelPath, obj.getValue()), getContext());
                        }

                    } catch (IndexingException e) {
                        System.out.println("[Error-IndexingException] Possible reason: " + e.getMessage());
                    }
                }

            } else if (message instanceof ResultSet) {

                ResultSet r = (ResultSet) message;
                numAnswers++;
                partResults.add(r);

                if (numAnswers >= possibleAnswers) {

                    results = combiner.intersect(partResults, node.limit);

                    if (originalActor != null) {
                        originalActor.sendOneWay(results);
                    } else {
                        future.completeWithResult(results);
                    }
                }
            }
        }
    }

    @Override
    public ResultSet process(QueryParserNode node) {

        ActorRef runningActor = null;
        if (node.predicateType.equals(And.class)
                || node.predicateType.equals(Or.class)) {
            runningActor = UntypedActor.actorOf(new UntypedActorFactory() {

                @Override
                public UntypedActor create() {
                    return new BooleanSearcherActor();
                }
            }).start();
        } else if (node.predicateType.equals(Similar.class)
                || node.predicateType.equals(Equal.class)
                || node.predicateType.equals(NotEqual.class)) {
            runningActor = UntypedActor.actorOf(new UntypedActorFactory() {

                @Override
                public UntypedActor create() {
                    return new SimilarEqualParallelSearcherActor();
                }
            }).start();
        }

        long begin = Calendar.getInstance().getTimeInMillis();

        //waiting for the processor to output results
        Future future = runningActor.sendRequestReplyFuture(node, Long.MAX_VALUE, null);
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
                System.out.println("Error: Result wasn't defined. Something went wrong.");
            }
        }

        //something went wrong so return an empty result set
        return new ResultSetDefaultImpl<E>();
    }
}
