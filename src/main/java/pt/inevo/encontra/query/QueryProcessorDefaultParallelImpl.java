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
* Default implementation for the query processor.
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
     * Searches for a simple query in the given searcher.
     * It's just used to encapsulate the call to the specific searcher.
     */
    class SimpleParallelSearcherActor extends UntypedActor {

        protected Searcher searcher;
        protected ResultSet results;

        public SimpleParallelSearcherActor(Searcher searcher) {
            this.searcher = searcher;
        }

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof Query) {
                //searches the query
                Query query = (Query) message;
                results = searcher.search(query);

                //return the results
                getContext().replySafe(results);
            }
        }
    }

    class BooleanSearcherActor extends UntypedActor {

        protected HashMap<String, ActorRef> searchActors;
        protected int numAnswers, possibleAnswers;
        protected List results;
        protected ResultSetOperations combiner;
        protected ActorRef originalActor;
        protected CompletableFuture future;
        protected QueryParserNode node;
        protected int originalLimit = 0;

        public BooleanSearcherActor(HashMap<String, ActorRef> actors) {
            this.searchActors = actors;
            combiner = new ResultSetOperations();
            results = new ArrayList<ResultSetDefaultImpl>();
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
                originalLimit = node.limit;
                List<QueryParserNode> nodes = node.childrenNodes;

                //perform simple queries first
                Collections.sort(nodes, new Comparator<QueryParserNode>() {

                    @Override
                    public int compare(QueryParserNode o1, QueryParserNode o2) {
                        if (o1.predicateType.equals(And.class) || o1.predicateType.equals(Or.class)) {
                            return 1;

                        } else if (o2.predicateType.equals(And.class) || o2.predicateType.equals(Or.class)) {
                            return -1;
                        } else {
                            return 0;

                        }
                    }
                });

                possibleAnswers = nodes.size();
                for (QueryParserNode n : nodes) {
                    if (n.predicateType.equals(Similar.class)
                            || n.predicateType.equals(Equal.class)
                            || n.predicateType.equals(NotEqual.class)) {

                        ActorRef actorRef = UntypedActor.actorOf(new UntypedActorFactory() {

                            @Override
                            public UntypedActor create() {
                                return new SimilarEqualParallelSearcherActor(searchActors);
                            }
                        }).start();

                        actorRef.sendOneWay(n, getContext());
                    } else if (n.predicateType.equals(And.class)
                            || n.predicateType.equals(Or.class)) {
                        ActorRef andActorRef = UntypedActor.actorOf(new UntypedActorFactory() {

                            @Override
                            public UntypedActor create() {
                                return new BooleanSearcherActor(searchActors);
                            }
                        }).start();

                        andActorRef.sendOneWay(n, getContext());
                    }
                }
            } else if (message instanceof ResultSet) {

                ResultSet r = (ResultSet) message;
                numAnswers++;
                results.add(r);

                if (numAnswers >= possibleAnswers) {

                    ResultSet combinedResultSet;

                    if (node.predicateType.equals(And.class)) {
                        combinedResultSet = combiner.intersect(results, originalLimit);
                    } else {
                        combinedResultSet = combiner.join(results, node.distinct, originalLimit);
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

        protected HashMap<String, ActorRef> searchActors;
        protected int numAnswers, possibleAnswers;
        protected List partResults;
        protected ResultSet results;
        protected ResultSetOperations combiner;
        protected ActorRef originalActor;
        protected CompletableFuture future;
        protected QueryParserNode node;

        public SimilarEqualParallelSearcherActor(HashMap<String, ActorRef> actors) {
            this.searchActors = actors;
            combiner = new ResultSetOperations();
            partResults = new ArrayList<ResultSetDefaultImpl>();
        }

        //Create a subquery for Equal, Similar and NoEqual, given a node
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

                        possibleAnswers = 1;
                        ActorRef searcher = searchActors.get(parentField).start();
                        searcher.sendOneWay(createSubQuery(node, newQueryPath, node.fieldObject), getContext());

                    } else {
                        possibleAnswers = 1;
                        ActorRef searcher = searchActors.get(node.field).start();
                        searcher.sendOneWay(createSubQuery(node, parentPath, node.fieldObject), getContext());
                    }
                } else {
                    //dont know which searchers to use, so lets digg a bit
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

                            ActorRef searcher = searchActors.get(fieldName).start();
                            searcher.sendOneWay(createSubQuery(node, subModelPath, obj.getValue()), getContext());
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

    class ParallelQueryProcessor extends UntypedActor {

        HashMap<String, ActorRef> searchActors;
        ResultSetOperations combiner = new ResultSetOperations();
        List resultsParts = new ArrayList<ResultSetDefaultImpl<E>>();
        ActorRef originalActor;
        CompletableFuture future;

        ParallelQueryProcessor(final Map<String, Searcher> searchers) {
            searchActors = new HashMap<String, ActorRef>();
            for (String s : searchers.keySet()) {

                final String searcherName = s;
                ActorRef actorRef = UntypedActor.actorOf(new UntypedActorFactory() {

                    @Override
                    public UntypedActor create() {
                        return new SimpleParallelSearcherActor(searchers.get(searcherName));
                    }
                }).start();

                searchActors.put(s, actorRef);
            }
        }

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof QueryParserNode) {
                QueryParserNode node = (QueryParserNode) message;

                if (getContext().getSenderFuture().isDefined()) {
                    future = (CompletableFuture) getContext().getSenderFuture().get();
                } else if (getContext().getSender().isDefined()) {
                    originalActor = (ActorRef) getContext().getSender().get();
                }

                if (node.predicateType.equals(And.class)
                        || node.predicateType.equals(Or.class)) {
                    ActorRef andActorRef = UntypedActor.actorOf(new UntypedActorFactory() {

                        @Override
                        public UntypedActor create() {
                            return new BooleanSearcherActor(searchActors);
                        }
                    }).start();

                    andActorRef.sendOneWay(message, getContext());
                } else if (node.predicateType.equals(Similar.class)
                        || node.predicateType.equals(Equal.class)
                        || node.predicateType.equals(NotEqual.class)) {
                    ActorRef actorRef = UntypedActor.actorOf(new UntypedActorFactory() {

                        @Override
                        public UntypedActor create() {
                            return new SimilarEqualParallelSearcherActor(searchActors);
                        }
                    }).start();

                    actorRef.sendOneWay(message, getContext());
                }

            } else if (message instanceof ResultSet) {
                ResultSet set = (ResultSet) message;

                if (originalActor != null) {
                    originalActor.sendOneWay(set);
                } else {
                    future.completeWithResult(set);
                }

            }
        }
    }

    @Override
    public ResultSet process(QueryParserNode node) {

        ResultSet<E> results = new ResultSetDefaultImpl<E>();

        //creating the parallel query processor actor
        ActorRef actorRef = UntypedActor.actorOf(new UntypedActorFactory() {

            @Override
            public UntypedActor create() {
                return new ParallelQueryProcessor(searcherMap);
            }
        }).start();

        //waiting for the processor to output results
        Future future = actorRef.sendRequestReplyFuture(node, Long.MAX_VALUE, null);
        future.await();

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

        return results;
    }
}
