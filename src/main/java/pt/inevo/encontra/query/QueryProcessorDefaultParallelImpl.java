package pt.inevo.encontra.query;

import akka.actor.UntypedActorFactory;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.dispatch.CompletableFuture;
import akka.dispatch.Future;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import pt.inevo.encontra.engine.QueryProcessor;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.IndexingException;
import pt.inevo.encontra.index.Result;
import pt.inevo.encontra.index.ResultSet;
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
    ResultSetParallelOperations combiner;

    public QueryProcessorDefaultParallelImpl() {
        super();
        combiner = new ResultSetParallelOperations();
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

    class AndParallelSearcherActor extends UntypedActor {

        protected HashMap<String, ActorRef> searchActors;
        protected int numAnswers, possibleAnswers;
        protected ResultSet andResults;
        protected ResultSetParallelOperations combiner;
        protected ActorRef originalActor;
        protected CompletableFuture future;

        public AndParallelSearcherActor(HashMap<String, ActorRef> actors) {
            this.searchActors = actors;
            combiner = new ResultSetParallelOperations();
            andResults = new ResultSet();
        }

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof QueryParserNode) {
                if (getContext().getSenderFuture().isDefined()) {
                    future = (CompletableFuture) getContext().getSenderFuture().get();
                } else if (getContext().getSender().isDefined()) {
                    originalActor = (ActorRef) getContext().getSender().get();
                }

                QueryParserNode node = (QueryParserNode) message;
                List<QueryParserNode> nodes = node.childrenNodes;
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
                    } else if (n.predicateType.equals(And.class)) {
                        ActorRef andActorRef = UntypedActor.actorOf(new UntypedActorFactory() {

                            @Override
                            public UntypedActor create() {
                                return new AndParallelSearcherActor(searchActors);
                            }
                        }).start();

                        andActorRef.sendOneWay(n, getContext());
                    } else if (n.predicateType.equals(Or.class)) {
                        ActorRef orActorRef = UntypedActor.actorOf(new UntypedActorFactory() {

                            @Override
                            public UntypedActor create() {
                                return new OrParallelSearcherActor(searchActors);
                            }
                        }).start();

                        orActorRef.sendOneWay(n, getContext());
                    }
                }
            } else if (message instanceof ResultSet) {

                ResultSet r = (ResultSet) message;
                numAnswers++;
                if (numAnswers == 1) {
                    andResults = r;
                }
                else
                    andResults = combiner.intersect(andResults, r);

                if (numAnswers >= possibleAnswers) {
                    if (originalActor != null) {
                        originalActor.sendOneWay(andResults);
                    } else {
                        future.completeWithResult(andResults);
                    }
                }
            }
        }
    }

    class OrParallelSearcherActor extends UntypedActor {

        protected HashMap<String, ActorRef> searchActors;
        protected int numAnswers, possibleAnswers;
        protected List<ResultSet> orResults;
        protected ResultSet results;
        protected ResultSetParallelOperations combiner;
        protected ActorRef originalActor;
        protected CompletableFuture future;

        public OrParallelSearcherActor(HashMap<String, ActorRef> actors) {
            this.searchActors = actors;
            combiner = new ResultSetParallelOperations();
            orResults = new ArrayList<ResultSet>();
        }

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof QueryParserNode) {
                if (getContext().getSenderFuture().isDefined()) {
                    future = (CompletableFuture) getContext().getSenderFuture().get();
                } else if (getContext().getSender().isDefined()) {
                    originalActor = (ActorRef) getContext().getSender().get();
                }

                QueryParserNode node = (QueryParserNode) message;
                List<QueryParserNode> nodes = node.childrenNodes;
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
                    } else if (n.predicateType.equals(And.class)) {
                        ActorRef andActorRef = UntypedActor.actorOf(new UntypedActorFactory() {

                            @Override
                            public UntypedActor create() {
                                return new AndParallelSearcherActor(searchActors);
                            }
                        }).start();

                        andActorRef.sendOneWay(n, getContext());
                    } else if (n.predicateType.equals(Or.class)) {
                        ActorRef orActorRef = UntypedActor.actorOf(new UntypedActorFactory() {

                            @Override
                            public UntypedActor create() {
                                return new OrParallelSearcherActor(searchActors);
                            }
                        }).start();

                        orActorRef.sendOneWay(n, getContext());
                    }
                }
            } else if (message instanceof ResultSet) {

                ResultSet r = (ResultSet) message;
                orResults.add(r);
                numAnswers++;

                if (numAnswers >= possibleAnswers) {

                    //must check if it is distinct or not
                    results = combiner.join(orResults, true);

                    if (originalActor != null) {
                        originalActor.sendOneWay(results);
                    } else {
                        future.completeWithResult(results);
                    }
                }
            }
        }
    }

    class SimilarEqualParallelSearcherActor extends UntypedActor {

        protected HashMap<String, ActorRef> searchActors;
        protected int numAnswers, possibleAnswers;
        protected List<ResultSet> partResults;
        protected ResultSet results;
        protected ResultSetParallelOperations combiner;
        protected ActorRef originalActor;
        protected CompletableFuture future;

        public SimilarEqualParallelSearcherActor(HashMap<String, ActorRef> actors) {
            this.searchActors = actors;
            combiner = new ResultSetParallelOperations();
            partResults = new ArrayList<ResultSet>();
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

                QueryParserNode node = (QueryParserNode) message;

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
//                        Searcher s = searcherMap.get(parentField);
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
                            Searcher s = searcherMap.get(fieldName);

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

                    results = combiner.intersect(partResults);

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
        ResultSetParallelOperations combiner = new ResultSetParallelOperations();
        List<ResultSet<E>> resultsParts = new ArrayList<ResultSet<E>>();
        ActorRef originalActor;
        CompletableFuture future;
        int count = 0;

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

                if (node.predicateType.equals(And.class)) {
                    ActorRef andActorRef = UntypedActor.actorOf(new UntypedActorFactory() {

                        @Override
                        public UntypedActor create() {
                            return new AndParallelSearcherActor(searchActors);
                        }
                    }).start();

                    andActorRef.sendOneWay(message, getContext());
                } else if (node.predicateType.equals(Or.class)) {
                    ActorRef orActorRef = UntypedActor.actorOf(new UntypedActorFactory() {

                        @Override
                        public UntypedActor create() {
                            return new OrParallelSearcherActor(searchActors);
                        }
                    }).start();

                    orActorRef.sendOneWay(message, getContext());
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

        ResultSet<E> results = new ResultSet<E>();

        //creating the parallel query processor actor
        ActorRef actorRef = UntypedActor.actorOf(new UntypedActorFactory() {

            @Override
            public UntypedActor create() {
                return new ParallelQueryProcessor(searcherMap);
            }
        }).start();

        //waiting for the processor to output results
        Future future = actorRef.sendRequestReplyFuture(node, 300000, null);
        future.await();

        if (future.isCompleted()) {
            Option resultOption = future.result();
            if (resultOption.isDefined()) {
                //everything is ok, so the results were retrieved
                Object result = resultOption.get();
                return (ResultSet) result;
            } else {
                //problem -> something went wrong, must check what happened
                System.out.println("Error here.");
            }
        }

//          if (node.predicateType.equals(Similar.class)
//                || node.predicateType.equals(Equal.class)
//                || node.predicateType.equals(NotEqual.class)) {

//            if (node.field != null) {
//                /*
//                 * must check the path object - see wheter it is a field from this
//                 * class or from other indexed field, in other class
//                 */
//                QueryParserNode pathNode = node.childrenNodes.get(0);
//                Path p = (Path) pathNode.predicate;
//
//                //track the full path of the desired field
//                Queue<Path> relativePaths = new LinkedList<Path>();
//
//                Path parentPath = p.getParentPath();
//                if (parentPath.isField()) {     //its not a field of this processor
//                    while (parentPath.isField()) {
//                        relativePaths.add(parentPath);
//                        parentPath = parentPath.getParentPath();
//                    }
//
//                    if (relativePaths.size() > 0) {
//                        parentPath = relativePaths.remove();
//                    } else {
//                        parentPath = p;
//                    }
//
//                    String parentField = parentPath.getAttributeName();
//                    Searcher s = searcherMap.get(parentField);
//                    Class clazz = parentPath.getJavaType();
//                    Path newQueryPath = new Path(clazz);
//
//                    for (Path relPath : relativePaths) {
//                        newQueryPath = newQueryPath.get(relPath.getAttributeName());
//                    }
//
//                    //in the end we must have the elements we desire
//                    newQueryPath = newQueryPath.get(node.field);
//                    return s.search(createSubQuery(node, newQueryPath, node.fieldObject));
//
//                } else {
//                    //get the respective searcher
//                    Searcher s = searcherMap.get(node.field);
//                    results = s.search(createSubQuery(node, parentPath, node.fieldObject));
//                }
//            } else {
//                //dont know which searchers to use, so lets digg a bit
//                try {
//                    List<IndexedObject> indexedObjects = indexedObjectFactory.processBean((IEntity) node.fieldObject);
//                    List<ResultSet<E>> resultsParts = new ArrayList<ResultSet<E>>();
//                    for (IndexedObject obj : indexedObjects) {
//                        String fieldName = obj.getName();
//                        Searcher s = searcherMap.get(fieldName);
//
//                        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
//
//                        CriteriaQuery query = cb.createQuery(obj.getValue().getClass());
//                        Path subModelPath = null;
//                        Class clazz = obj.getValue().getClass();
//                        //detect if the object is a compound one
//                        if (obj.getValue() instanceof IEntity || obj.getValue() instanceof IndexedObject) {
//                            clazz = obj.getValue().getClass();
//                            subModelPath = query.from(clazz);
//                        } else {
//                            clazz = node.fieldObject.getClass();
//                            subModelPath = query.from(clazz);
//                            subModelPath = subModelPath.get(fieldName);
//                        }
//
//                        resultsParts.add(s.search(createSubQuery(node, subModelPath, obj.getValue())));
//                    }
//
//                    results = combiner.intersect(resultsParts);
//
//                } catch (IndexingException e) {
//                    System.out.println("[Error-IndexingException] Possible reason: " + e.getMessage());
//                }
//            }
//        }

        results.sort();
        return results;
    }
}

/**
 * Implementation of Boolean Operations with ResultSets.
 * @author Ricardo
 * @param <E>
 */
class ResultSetParallelOperations<E> {

    /**
     * Applies boolean operation AND to the list of ResultSets.
     * @param results the list where to apply the AND operation
     * @return
     */
    public ResultSet<E> intersect(List<ResultSet<E>> results) {

        boolean first = true;
        ResultSet combinedResultSet = new ResultSet(), set1 = null, set2 = null;
        for (int i = 0; i < results.size(); i++) {

            if (first) {
                if (i + 1 < results.size()) {
                    set1 = results.get(i);
                    set2 = results.get(i + 1);
                    combinedResultSet = this.intersect(set1, set2);
                    i++;
                } else {
                    combinedResultSet = results.get(i);
                }
            } else {
                set1 = combinedResultSet;
                set2 = results.get(i);
                combinedResultSet = this.intersect(set1, set2);
            }
        }

        return combinedResultSet;
    }

    /**
     * Applies boolean operation OR to the list of ResultSets.
     * @param results the list where to apply the OR operation
     * @return
     */
    public ResultSet<E> join(List<ResultSet<E>> results, boolean distinct) {

        ResultSet combinedResultSet = new ResultSet();
        for (ResultSet set : results) {
            Iterator<Result> it = set.iterator();
            while (it.hasNext()) {
                Result r = it.next();
                if (distinct) {
                    if (!combinedResultSet.contains(r)) {
                        combinedResultSet.add(r);
                    }
                } else {
                    combinedResultSet.add(r);
                }
            }
        }

        return combinedResultSet;
    }

    /**
     * Brute force combination of two ResultSet's. Only Result's that appear on
     * both ResultSet's are included in the result ResultSet.
     * @param set1
     * @param set2
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public ResultSet intersect(ResultSet<?> set1, ResultSet set2) {

        List<Result> combinedResults = new ArrayList<Result>();

        for (Result r1 : set1) {
            if (set2.contains(r1)) {
                Result r2 = set2.get(set2.indexOf(r1));
                Result n = new Result(r1.getResult());
                n.setSimilarity(r1.getSimilarity() * r2.getSimilarity());
                combinedResults.add(n);
            }
        }

        return new ResultSet(combinedResults);
    }
}
