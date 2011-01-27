//package pt.inevo.encontra.query;
//
//import akka.actor.UntypedActorFactory;
//import akka.actor.ActorRef;
//import akka.actor.UntypedActor;
//import akka.dispatch.CompletableFuture;
//import akka.dispatch.Future;
//import java.lang.reflect.Constructor;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Queue;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import pt.inevo.encontra.engine.QueryProcessor;
//import pt.inevo.encontra.index.IndexedObject;
//import pt.inevo.encontra.index.IndexingException;
//import pt.inevo.encontra.common.Result;
//import pt.inevo.encontra.index.ResultSetDefaultImp;
//import pt.inevo.encontra.index.search.ResultsProvider;
//import pt.inevo.encontra.index.search.Searcher;
//import pt.inevo.encontra.query.criteria.CriteriaBuilderImpl;
//import pt.inevo.encontra.query.criteria.CriteriaQueryImpl;
//import pt.inevo.encontra.query.criteria.Expression;
//import pt.inevo.encontra.query.criteria.exps.And;
//import pt.inevo.encontra.query.criteria.exps.Equal;
//import pt.inevo.encontra.query.criteria.exps.NotEqual;
//import pt.inevo.encontra.query.criteria.exps.Or;
//import pt.inevo.encontra.query.criteria.exps.Similar;
//import pt.inevo.encontra.storage.IEntity;
//import scala.Option;
//
//class Message {
//
//    public String type;
//    public Object value;
//}
//
///**
// * Default implementation for the query processor.
// * @author Ricardo
// */
//public class QueryProcessorParallelLinearImpl<E extends IEntity> extends QueryProcessor<E> {
//
//    protected Class resultClass;
//    protected ActorRef parallelProcessor;
//    protected Logger logger;
//
//    public QueryProcessorParallelLinearImpl() {
//        super();
//        queryParser = new QueryParserDefaultImpl();
//        logger = LoggerFactory.getLogger(QueryProcessorParallelLinearImpl.class);
//    }
//
//    /**
//     * Just encapsulates the call to the specific "simple" searcher.
//     */
//    class SimpleParallelSearcherActor extends UntypedActor {
//
//        protected Searcher searcher;
//        protected ResultsProvider resultsProvider;
//
//        public SimpleParallelSearcherActor(Searcher searcher) {
//            this.searcher = searcher;
//        }
//
//        private void sendResult() {
//            Message answer = new Message();
//            answer.type = "EMPTY";
//            if (resultsProvider != null) {
//                Result res = resultsProvider.getNext();
//                if (res != null) {
//                    answer.type = "RESULT";
//                }
//                answer.value = res;
//            }
//            getContext().replySafe(answer);
//        }
//
//        @Override
//        public void onReceive(Object message) throws Exception {
//            Message m = (Message) message;
//            if (m.type.equals("GETNEXT")) {
//                sendResult();
//            } else if (m.type.equals("QUERY")) {
//                //searches the query
//                Query query = (Query) m.value;
//                resultsProvider = searcher.getResultsProvider(query);
//                //return the first result
//                sendResult();
//            }
//        }
//    }
//
//    class BooleanSearcherActor extends UntypedActor {
//
//        protected HashMap<String, ActorRef> searchActors;
//        protected ResultSetDefaultImp results;
//        protected ActorRef originalActor;
//        protected HashMap<ActorRef, ResultSetDefaultImp> partialResults;
//        protected ArrayList<ActorRef> emptyResults;
//        protected CompletableFuture future;
//        protected HashMap<ActorRef, Message> booleanActors;
//
//        public BooleanSearcherActor(HashMap<String, ActorRef> actors) {
//            this.searchActors = actors;
//            results = new ResultSetDefaultImp();
//            partialResults = new HashMap<ActorRef, ResultSetDefaultImp>();
//            emptyResults = new ArrayList<ActorRef>();
//        }
//
//        private void tryGetResult(String type) {
//            HashMap<ActorRef, Future> futures = new HashMap<ActorRef, Future>();
//                //send the queries to all the actors
//                for (ActorRef producer : booleanActors.keySet()) {
//                    if (type.equals("QUERY"))
//                        futures.put(producer, producer.sendRequestReplyFuture(booleanActors.get(producer), Long.MAX_VALUE, null));
//                    else {
//                        Message newMessage = new Message();
//                        newMessage.type = type;
//                        futures.put(producer, producer.sendRequestReplyFuture(newMessage, Long.MAX_VALUE, null));
//                    }
//                }
//                //wait for all the futures
//                for (Future f : futures.values()) {
//                    f.await();
//                }
//
//                ResultSetDefaultImp set = new ResultSetDefaultImp();
//                boolean noResultSent = false;
//                for (ActorRef actor : futures.keySet()) {
//                    Future f = futures.get(actor);
//                    boolean allHave = true;
//                    if (f.isCompleted()) {
//                        Option resultOption = f.result();
//                        if (resultOption.isDefined()) {
//                            Message res = (Message) resultOption.get();
//                            if (res.type.equals("RESULT")) {
//                                Result newResult = (Result) res.value;
//                                partialResults.get(actor).add(newResult);
//
//                                //so, we don't have yet this result in the list
//                                if (!results.contains(newResult)) {
//                                    for (ResultSetDefaultImp part : partialResults.values()) {
//                                        if (!part.contains(newResult)) {
//                                            allHave = false;
//                                            break;
//                                        }
//                                    }
//                                    if (allHave) {  //so add the new result to the list
//                                        results.add(newResult);
//                                        noResultSent = true;
//                                        set.add(newResult);
//                                    }
//                                }
//                            } else { //EMPTY so don't send any requests to it
//                                //TO DO don't send requests to this one
//                            }
//                        } else {
//                            System.out.println("Got not result!");
//                        }
//                    }
//                }
//
//                if (!noResultSent) {    //so let's jus re-try
//                    Message getNext = new Message();
//                    getNext.type = "GETNEXT";
//                    getContext().sendOneWay(getNext);
//                } else {
//                    Message answer = new Message();
//                    answer.type = "RESULT";
//                    answer.value = set;
//                    if (originalActor != null) {
//                        originalActor.sendOneWay(answer, originalActor);
//                    } else {
//                        future.completeWithResult(answer);
//                    }
//                }
//        }
//
//        @Override
//        public void onReceive(Object message) throws Exception {
//            Message m = (Message) message;
//            if (m.type.equals("QUERY")) {
//                if (getContext().getSender().isDefined()) {
//                    originalActor = (ActorRef) getContext().getSender().get();
//                } else if (getContext().getSenderFuture().isDefined()) {
//                    future = (CompletableFuture) getContext().getSenderFuture().get();
//                }
//
//                QueryParserNode node = (QueryParserNode) m.value;
//                List<QueryParserNode> nodes = node.childrenNodes;
//
//                booleanActors = new HashMap<ActorRef, Message>(nodes.size());
//                //create the necessary actors and send them the results
//                for (QueryParserNode n : nodes) {
//                    Message newMessage = new Message();
//                    newMessage.type = "QUERY";
//                    newMessage.value = n;
//                    if (n.predicateType.equals(Similar.class)
//                            || n.predicateType.equals(Equal.class)
//                            || n.predicateType.equals(NotEqual.class)) {
//
//                        ActorRef actorRef = UntypedActor.actorOf(new UntypedActorFactory() {
//
//                            @Override
//                            public UntypedActor create() {
//                                return new SimilarEqualParallelSearcherActor(searchActors);
//                            }
//                        }).start();
//                        partialResults.put(actorRef, new ResultSetDefaultImp());
//                        booleanActors.put(actorRef, newMessage); //associate the actor if the sub-query is responsible for
//                    } else if (n.predicateType.equals(And.class)
//                            || n.predicateType.equals(Or.class)) {
//                        ActorRef andActorRef = UntypedActor.actorOf(new UntypedActorFactory() {
//
//                            @Override
//                            public UntypedActor create() {
//                                return new BooleanSearcherActor(searchActors);
//                            }
//                        }).start();
//                        partialResults.put(andActorRef, new ResultSetDefaultImp());
//                        booleanActors.put(andActorRef, newMessage); //associate the actor if the sub-query is responsible for
//                    }
//                }
//
//                tryGetResult(m.type);
//            } else if (m.type.equals("GETNEXT")) {
//
//                if (getContext().getSenderFuture().isDefined()) {
//                    future = getContext().getSenderFuture().get();
//                }
//                tryGetResult(m.type);
//            }
//        }
//    }
//
//    class SimilarEqualParallelSearcherActor extends UntypedActor {
//
//        protected HashMap<String, ActorRef> searchActors;
//        protected List<ResultSetDefaultImp> partResults;
//        protected ResultSetDefaultImp results;
//        protected ActorRef originalActor;
//        protected CompletableFuture future;
//        protected ActorRef activeSearcher;
//        protected CompletableFuture activeSearcherFuture;
//
//        public SimilarEqualParallelSearcherActor(HashMap<String, ActorRef> actors) {
//            this.searchActors = actors;
//            partResults = new ArrayList<ResultSetDefaultImp>();
//        }
//
//        //Create a subquery for Equal, Similar and NoEqual, given a node
//        private Query createSubQuery(QueryParserNode node, Path path, Object obj) {
//            CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
//            CriteriaQuery q = cb.createQuery(resultClass);
//            try {
//                Constructor c = node.predicateType.getConstructor(Expression.class, Object.class);
//                CriteriaQuery newQuery = q.where((Expression) c.newInstance(path, obj));
//                newQuery = ((CriteriaQueryImpl) newQuery).distinct(node.distinct);
//                return newQuery;
//            } catch (Exception ex) {
//                System.out.println("[Error]: Could not execute the query! Possible reason: " + ex.getMessage());
//            }
//            return q;
//        }
//
//        @Override
//        public void onReceive(Object message) throws Exception {
//            Message m = (Message) message;
//            if (m.type.equals("QUERY")) {
//                if (getContext().getSenderFuture().isDefined()) {
//                    future = (CompletableFuture) getContext().getSenderFuture().get();
//                } else if (getContext().getSender().isDefined()) {
//                    originalActor = (ActorRef) getContext().getSender().get();
//                }
//
//                QueryParserNode node = (QueryParserNode) m.value;
//
//                if (node.field != null) {
//                    /*
//                     * must check the path object - see wheter it is a field from this
//                     * class or from other indexed field, in other class
//                     */
//                    QueryParserNode pathNode = node.childrenNodes.get(0);
//                    Path p = (Path) pathNode.predicate;
//
//                    //track the full path of the desired field
//                    Queue<Path> relativePaths = new LinkedList<Path>();
//
//                    Path parentPath = p.getParentPath();
//                    if (parentPath.isField()) {     //its not a field of this processor
//                        while (parentPath.isField()) {
//                            relativePaths.add(parentPath);
//                            parentPath = parentPath.getParentPath();
//                        }
//
//                        if (relativePaths.size() > 0) {
//                            parentPath = relativePaths.remove();
//                        } else {
//                            parentPath = p;
//                        }
//
//                        String parentField = parentPath.getAttributeName();
//                        Class clazz = parentPath.getJavaType();
//                        Path newQueryPath = new Path(clazz);
//
//                        for (Path relPath : relativePaths) {
//                            newQueryPath = newQueryPath.get(relPath.getAttributeName());
//                        }
//
//                        //in the end we must have the elements we desire
//                        newQueryPath = newQueryPath.get(node.field);
//
//                        activeSearcher = searchActors.get(parentField).start();
//
//                        Message newMessage = new Message();
//                        newMessage.type = "QUERY";
//                        newMessage.value = createSubQuery(node, newQueryPath, node.fieldObject);
//                        activeSearcher.sendOneWay(newMessage, getContext());
//
//                    } else {
//                        activeSearcher = searchActors.get(node.field).start();
//
//                        Message newMessage = new Message();
//                        newMessage.type = "QUERY";
//                        newMessage.value = createSubQuery(node, parentPath, node.fieldObject);
//                        activeSearcher.sendOneWay(newMessage, getContext());
//                    }
//                } else {
//                    //dont know which searchers to use, so lets digg a bit
//                    try {
//                        List<IndexedObject> indexedObjects = indexedObjectFactory.processBean((IEntity) node.fieldObject);
//
//                        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
//                        Predicate predicate = cb.createQuery().getRestrictions();
//
//                        for (IndexedObject obj : indexedObjects) {
//                            String fieldName = obj.getName();
//
//                            CriteriaQuery query = cb.createQuery(obj.getValue().getClass());
//                            Path subModelPath = null;
//                            Class clazz = obj.getValue().getClass();
//                            //detect if the object is a compound one
//                            if (obj.getValue() instanceof IEntity || obj.getValue() instanceof IndexedObject) {
//                                clazz = obj.getValue().getClass();
//                                subModelPath = query.from(clazz);
//                            } else {
//                                clazz = node.fieldObject.getClass();
//                                subModelPath = query.from(clazz);
//                                subModelPath = subModelPath.get(fieldName);
//                            }
//
//                            CriteriaQuery q = (CriteriaQuery) createSubQuery(node, subModelPath, obj.getValue());
//                            if (predicate != null) {
//                                predicate = cb.and(predicate, q.getRestrictions());
//                            } else {
//                                predicate = q.getRestrictions();
//                            }
//
//                        }
//
//                        //just sets the actor to be a proxy for a Boolean Operator
//                        activeSearcher = UntypedActor.actorOf(new UntypedActorFactory() {
//
//                            @Override
//                            public UntypedActor create() {
//                                return new BooleanSearcherActor(searchActors);
//                            }
//                        }).start();
//
//                        Message newMessage = new Message();
//                        newMessage.type = "QUERY";
//                        newMessage.value = queryParser.parse(cb.createQuery().where(predicate));
//                        activeSearcher.sendOneWay(newMessage, getContext());
//
//                    } catch (IndexingException e) {
//                        System.out.println("[Error-IndexingException] Possible reason: " + e.getMessage());
//                    }
//                }
//
//            } else if (m.type.equals("RESULT") || m.type.equals("EMPTY")) {
//                //just repassed the message
//                if (originalActor != null) {
//                    originalActor.sendOneWay(m, getContext());
//                } else {
//                    future.completeWithResult(m);
//                }
//            } else if (m.type.equals("GETNEXT")) {
//                if (getContext().getSenderFuture().isDefined()) {
//                    future = (CompletableFuture) getContext().getSenderFuture().get();
//                } else if (getContext().getSender().isDefined()) {
//                    originalActor = (ActorRef) getContext().getSender().get();
//                }
//                activeSearcher.sendOneWay(m, getContext());
//            }
//        }
//    }
//
//    class ParallelQueryProcessor extends UntypedActor {
//
//        //Associates a simple search actor for each existent searcher
//        protected HashMap<String, ActorRef> searchActors;
//        //used to track the sender of the original query
//        protected CompletableFuture future;
//        //the results of the query executed
//        protected ResultSetDefaultImp<E> results;
//
//        ParallelQueryProcessor() {
//            //creating "simple" searcher actors for the low level queries
//            results = new ResultSetDefaultImp<E>();
//            searchActors = new HashMap<String, ActorRef>();
//            for (String s : searcherMap.keySet()) {
//                final String searcherName = s;
//                ActorRef actorRef = UntypedActor.actorOf(new UntypedActorFactory() {
//
//                    @Override
//                    public UntypedActor create() {
//                        return new SimpleParallelSearcherActor(searcherMap.get(searcherName));
//                    }
//                }).start();
//                searchActors.put(s, actorRef);
//            }
//        }
//
//        @Override
//        public void onReceive(Object message) throws Exception {
//            Message m = (Message) message;
//            if (m.type.equals("QUERY")) {
//                QueryParserNode node = (QueryParserNode) m.value;
//
//                if (getContext().getSenderFuture().isDefined()) {
//                    future = (CompletableFuture) getContext().getSenderFuture().get();
//                }
//
//                results = new ResultSetDefaultImp<E>();
//
//                ActorRef mainSearcher = null;
//
//                if (node.predicateType.equals(And.class)
//                        || node.predicateType.equals(Or.class)) {
//                    mainSearcher = UntypedActor.actorOf(new UntypedActorFactory() {
//
//                        @Override
//                        public UntypedActor create() {
//                            return new BooleanSearcherActor(searchActors);
//                        }
//                    }).start();
//                } else if (node.predicateType.equals(Similar.class)
//                        || node.predicateType.equals(Equal.class)
//                        || node.predicateType.equals(NotEqual.class)) {
//                    mainSearcher = UntypedActor.actorOf(new UntypedActorFactory() {
//
//                        @Override
//                        public UntypedActor create() {
//                            return new SimilarEqualParallelSearcherActor(searchActors);
//                        }
//                    }).start();
//                }
//
//                Future requestAnswer = mainSearcher.sendRequestReplyFuture(m, Long.MAX_VALUE, null);
//                requestAnswer.await();
//
//                if (requestAnswer.isCompleted()) {
//                    Option resultOption = requestAnswer.result();
//                    if (resultOption.isDefined()) {
//                        Message answerMessage = (Message) resultOption.get();
//                        for (; !answerMessage.type.equals("EMPTY") && results.size() < 20;) {
//
//                            if (requestAnswer.isCompleted()) {
//                                resultOption = requestAnswer.result();
//                                if (resultOption.isDefined()) {
//                                    answerMessage = (Message) resultOption.get();
//                                    if (answerMessage.value instanceof ResultSetDefaultImp) {
//                                        ResultSetDefaultImp<Result> resultList = (ResultSetDefaultImp<Result>) answerMessage.value;
//                                        for (Result r : resultList) {
//                                            results.add(r);
//                                        }
//                                    } else {
//                                        //so the message is RESULT (could be null, which is equal to empty)
//                                        Result newResult = (Result) answerMessage.value;
//                                        if (newResult != null) {
//                                            results.add(newResult);
//                                        } else {  //no more results, so just end the search
//                                            if (future != null) {   //TO DO check if it is necessary to pass a copy here
//                                                future.completeWithResult(results.getCopy());
//                                                break;
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                            //request another result or no results if we reach the end
//                            Message newMessage = new Message();
//                            newMessage.type = "GETNEXT";
//                            requestAnswer = mainSearcher.sendRequestReplyFuture(newMessage, Long.MAX_VALUE, null);
//                            requestAnswer.await();
//                        }
//
//                        if (answerMessage.type.equals("EMPTY") || results.size() == 20) {
//                            if (future != null) {
//                                future.completeWithResult(results.getCopy());
//
//                            }
//                        }
//                    } else {
//                        System.out.println("GOT NO RESULT! OOPS...");
//                    }
//                }
//
//            }
//        }
//    }
//
//    @Override
//    public ResultSetDefaultImp process(QueryParserNode node) {
//
//        ResultSetDefaultImp<E> results = new ResultSetDefaultImp<E>();
//        //creating the parallel query processor actor to improve performance
//        parallelProcessor = UntypedActor.actorOf(new UntypedActorFactory() {
//
//            @Override
//            public UntypedActor create() {
//                return new ParallelQueryProcessor();
//            }
//        }).start();
//
//        //set-up the message to be passed to the processor
//        Message query = new Message();
//        query.type = "QUERY";
//        query.value = node;
//
//        //waiting for the processor to output results
//        Future future = parallelProcessor.sendRequestReplyFuture(query, Long.MAX_VALUE, null);
//        future.await();
//
//        if (future.isCompleted()) {
//            Option resultOption = future.result();
//            if (resultOption.isDefined()) {
//                //everything is ok, so the results were retrieved
//                Object result = resultOption.get();
//                if (!(result instanceof ResultSetDefaultImp)) {
//                    logger.error("Processor returned results with wrong type.");
//                } else {
//                    results = (ResultSetDefaultImp) result;
//                    results.sort();
//                    logger.info("Results succefully retrieved.");
//                }
//            } else {
//                logger.error("Processor didn't return a result.");
//            }
//        }
//        return results;
//    }
//}
