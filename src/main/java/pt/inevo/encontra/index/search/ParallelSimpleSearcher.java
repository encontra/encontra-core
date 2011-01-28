package pt.inevo.encontra.index.search;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.dispatch.CompletableFuture;
import akka.dispatch.Future;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import pt.inevo.encontra.descriptors.Descriptor;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.index.EntryProvider;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.common.ResultSetDefaultImpl;
import pt.inevo.encontra.query.QueryParserNode;
import pt.inevo.encontra.query.Query;
import pt.inevo.encontra.query.QueryProcessorDefaultImpl;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.criteria.exps.Equal;
import pt.inevo.encontra.query.criteria.exps.NotEqual;
import pt.inevo.encontra.query.criteria.exps.Similar;
import pt.inevo.encontra.storage.IEntity;
import pt.inevo.encontra.storage.IEntry;

/**
 * Parallel Simple searcher
 */
public class ParallelSimpleSearcher<O extends IEntity> extends AbstractSearcher<O> {

    protected DescriptorExtractor extractor;
    protected ResultSet<Descriptor> resultList;

    class SimpleResultsProvider implements ResultsProvider<O> {

        String iteratorType;
        Descriptor queryDescriptor;
        EntryProvider<Descriptor> provider;
        ResultSet<Descriptor> iteratorList;
        Iterator<Result<Descriptor>> resultIt;
        Descriptor previousDescriptor;

        //the iterator position - this is very important!!!!
        SimpleResultsProvider(String queryType, Descriptor d) {
            iteratorType = queryType;
            queryDescriptor = d;
            provider = index.getEntryProvider();
            provider.setCursor(d);

            //let's start with 20 - is it enough?
            iteratorList = new ResultSetDefaultImpl<Descriptor>(new Result(d), 20);
            while (provider.hasNext()) {
                Descriptor desc = provider.getNext();
                if (!iteratorList.containsResultObject(desc)) {
                    //insert only if it doesn't already exists
                    Result newResult = new Result(desc);
                    newResult.setScore(d.getDistance(desc));
                    if (!iteratorList.add(newResult)) {
                        /*we are not improving the resultList going
                        this way, so stop the search*/
                        break;
                    }
                }
            }
            resultIt = iteratorList.iterator();
        }

        @Override
        public Result<O> getNext() {
            if (iteratorType.equals("SIMILAR")) {
                if (!resultIt.hasNext()) {
                    int oldSize = iteratorList.getSize();
                    iteratorList.setMaxSize(oldSize + 20);
                    resultIt = iteratorList.iterator();
                    for (int i = 0; i < oldSize && resultIt.hasNext(); i++) {
                        resultIt.next();
                    }

                    //just start looking from here
                    provider.setCursor(previousDescriptor);
                    while (provider.hasNext()) {
                        Descriptor desc = provider.getNext();
                        if (!iteratorList.containsResultObject(desc)) {
                            //insert only if it doesn't already exists
                            Result newResult = new Result(desc);
                            newResult.setScore(queryDescriptor.getDistance(desc));
                            if (!iteratorList.add(newResult)) {
                                /*we are not improving the resultList going
                                this way, so stop the search*/
                                break;
                            }
                        }
                    }

                }

                //this is a very horrible hacking :(
                if (resultIt.hasNext()) {
                    Descriptor descr = resultIt.next().getResultObject();
                    previousDescriptor = descr;
                    Result<Descriptor> result = new Result<Descriptor>(descr);
                    result.setScore(descr.getDistance(queryDescriptor)); // TODO - This is distance not similarity!!!

                    Result<O> r = new Result<O>((O) getDescriptorExtractor().getIndexedObject((Descriptor) result.getResultObject()));
                    r.setScore(result.getScore());
                    return r;
                } else {
                    //no more results to retrieve
                    return null;
                }

            }
            //must be carefull so this null doesn't pop out
            return null;
        }

        @Override
        public List<Result<O>> getNext(int next) {
            return null;
        }
    }

    @Override
    public ResultsProvider<O> getResultsProvider(Query query) {
        if (query instanceof CriteriaQuery) {
            //parse the query
            QueryParserNode node = queryProcessor.getQueryParser().parse(query);
            //make the query
            if (node.predicateType.equals(Similar.class)) {
                Descriptor d = getDescriptorExtractor().extract(new IndexedObject(null, node.fieldObject));
                return new SimpleResultsProvider("SIMILAR", d);
            } else if (node.predicateType.equals(Equal.class)) {
                Descriptor d = getDescriptorExtractor().extract(new IndexedObject(null, node.fieldObject));
                return new SimpleResultsProvider("EQUAL", d);
            } else if (node.predicateType.equals(NotEqual.class)) {
                Descriptor d = getDescriptorExtractor().extract(new IndexedObject(null, node.fieldObject));
                return new SimpleResultsProvider("NOTEQUAL", d);
            } else {
                return null;
            }
        }
        return null;
    }

    public ParallelSimpleSearcher() {
        queryProcessor = new QueryProcessorDefaultImpl();
    }

    public void setDescriptorExtractor(DescriptorExtractor extractor) {
        this.extractor = extractor;
    }

    public DescriptorExtractor getDescriptorExtractor() {
        return extractor;
    }

    @Override
    public boolean insert(O entry) {
        assert (entry != null);
        Descriptor descriptor = extractor.extract(entry);
        return index.insert(descriptor);
    }

    @Override
    public boolean remove(O entry) {
        assert (entry != null);
        Descriptor descriptor = extractor.extract(entry);
        return index.remove(descriptor);
    }

    @Override
    public ResultSet<O> search(Query query) {
        ResultSet<IEntry> results = new ResultSetDefaultImpl<IEntry>();

        if (query instanceof CriteriaQuery) {
            //parse the query
            QueryParserNode node = queryProcessor.getQueryParser().parse(query);
            //make the query
            if (node.predicateType.equals(Similar.class)) {
                Descriptor d = getDescriptorExtractor().extract(new IndexedObject(null, node.fieldObject));
                results = performKnnQuery(d, 10);
            } else if (node.predicateType.equals(Equal.class)) {
                Descriptor d = getDescriptorExtractor().extract(new IndexedObject(null, node.fieldObject));
                results = performEqualQuery(d, true);
            } else if (node.predicateType.equals(NotEqual.class)) {
                Descriptor d = getDescriptorExtractor().extract(new IndexedObject(null, node.fieldObject));
                results = performEqualQuery(d, false);
            } else {
                return getResultObjects(queryProcessor.search(query));
            }
        }

        return getResultObjects(results);
    }

    protected ResultSet<IEntry> performKnnQuery(Descriptor d, int maxHits) {

        ResultSet resultSet = new ResultSetDefaultImpl<Descriptor>();
        resultList = new ResultSetDefaultImpl(new Result(d), maxHits);

        ActorRef searchCoordinator = UntypedActor.actorOf(new UntypedActorFactory() {

            @Override
            public UntypedActor create() {
                return new SimpleSearchCoordinator();
            }
        }).start();

        Message m = new Message();
        m.operation = "SIMILAR";
        m.obj = d;

        Future future = searchCoordinator.sendRequestReplyFuture(m, Long.MAX_VALUE, null);
        future.await();

        if (future.isCompleted()) {
            for (Result<Descriptor> descr : resultList) {
                Result<Descriptor> result = new Result<Descriptor>(descr.getResultObject());
                result.setScore(descr.getResultObject().getDistance(d)); // TODO - This is distance not similarity!!!
                resultSet.add(result);
            }

            resultSet.normalizeScores();
            resultSet.invertScores(); // This is a distance (dissimilarity) and we need similarity
        }
        return resultSet;
    }

    protected ResultSet<IEntry> performEqualQuery(Descriptor d, boolean equal) {

        ResultSet resultSet = new ResultSetDefaultImpl<Descriptor>();
        if (!equal) {
            resultList = new ResultSetDefaultImpl(new Result(d), index.getEntryProvider().size());
        } else {
            resultList = new ResultSetDefaultImpl(new Result(d), 1);
        }

        ActorRef searchCoordinator = UntypedActor.actorOf(new UntypedActorFactory() {

            @Override
            public UntypedActor create() {
                return new SimpleSearchCoordinator();
            }
        }).start();

        Message m = new Message();
        m.operation = "EQUAL";
        m.obj = d;
        m.equal = equal;

        Future future = searchCoordinator.sendRequestReplyFuture(m, Long.MAX_VALUE, null);
        future.await();

        if (future.isCompleted()) {
            for (Result<Descriptor> descr : resultList) {
                Result<Descriptor> result = new Result<Descriptor>(descr.getResultObject());
                result.setScore(descr.getResultObject().getDistance(d)); // TODO - This is distance not similarity!!!
                resultSet.add(result);
            }

            resultSet.normalizeScores();
            resultSet.invertScores(); // This is a distance (dissimilarity) and we need similarity
        }
        return resultSet;
    }

    @Override
    protected Result<O> getResultObject(Result<IEntry> indexEntryresult) {
        return new Result<O>((O) getDescriptorExtractor().getIndexedObject((Descriptor) indexEntryresult.getResultObject()));
    }

    //Message to be passed between actors
    class Message {

        public String operation;
        public int posInit;
        public int posEnd;
        public Object obj;
        public boolean equal;
    }

    class SimpleSearcherActor extends UntypedActor {

        protected EntryProvider<Descriptor> provider;
        protected int status;

        SimpleSearcherActor(EntryProvider<Descriptor> provider) {
            this.provider = provider;
        }

        @Override
        public void onReceive(Object o) throws Exception {
            Message message = (Message) o;

            provider.begin();
            if (message.posInit != 0) {
                for (int i = 0; i < message.posInit; i++) {
                    if (provider.hasNext()) {
                        provider.getNext();
                    }
                }
            }

            status = message.posEnd - message.posInit;

            if (message.operation.equals("SIMILAR")) {
                while (provider.hasNext() && status != 0) {
                    Descriptor p = provider.getNext();
                    Result newResult = new Result(p);
                    if (!resultList.containsResultObject(p)) {
                        //insert only if it doesn't already exists
                        if (!resultList.add(newResult)) {
                            /*we are not improving the resultList going
                            this way, so stop the search*/
                        }
                    }
                    status--;
                }

                Message m = new Message();
                m.operation = "FINISHED";
                getContext().replySafe(m);

            } else if (message.operation.equals("EQUAL")) {
                Descriptor d = (Descriptor) message.obj;
                while (provider.hasNext() && status != 0) {
                    Descriptor desc = provider.getNext();
                    double distance = d.getDistance(desc);
                    // calculate the overall max distance to normalize score afterwards
                    if (message.equal && distance == 0) {
                        Result newResult = new Result(desc);
                        newResult.setScore(distance);
                        resultList.add(newResult);
                        break;
                    } else if (!message.equal && distance != 0) {
                        Result newResult = new Result(desc);
                        newResult.setScore(distance);
                        resultList.add(newResult);
                    }
                }

                Message m = new Message();
                m.operation = "SUCCESS";
                getContext().replySafe(m);
            }
        }
    }

    class SimpleSearchCoordinator extends UntypedActor {

        protected int MAX_ACTORS = 10;
        protected int count = 0, posAct;
        protected ActorRef originalActor;
        protected CompletableFuture future;
        protected List<ActorRef> searchActors;
        protected EntryProvider provider;

        SimpleSearchCoordinator() {
            //create a group of several searchers through the index
            searchActors = new ArrayList<ActorRef>();
            for (int i = 0; i < MAX_ACTORS; i++) {
                final EntryProvider<Descriptor> provider = index.getEntryProvider();
                ActorRef searchActor = UntypedActor.actorOf(new UntypedActorFactory() {

                    @Override
                    public UntypedActor create() {
                        return new SimpleSearcherActor(provider);
                    }
                });
                searchActors.add(searchActor);
            }
        }

        @Override
        public void onReceive(Object o) throws Exception {
            Message message = (Message) o;
            if (message.operation.equals("SIMILAR")
                    || message.operation.equals("EQUAL")) {
                count = 0;
                if (getContext().getSenderFuture().isDefined()) {
                    future = (CompletableFuture) getContext().getSenderFuture().get();
                } else if (getContext().getSender().isDefined()) {
                    originalActor = (ActorRef) getContext().getSender().get();
                }

                //getting an entry provider to inspect the index
                provider = index.getEntryProvider();

                if (provider.size() > 10) { //sending 10 actors to search the index to improve performance
                    for (int i = 0; i < MAX_ACTORS && posAct < provider.size(); i++) {
                        Message m = new Message();
                        m.operation = message.operation;    //use the exact same operation
                        m.posInit = 0;
                        m.posInit = posAct;
                        if ((posAct + 10) < provider.size()) {
                            posAct += 10;
                        } else {
                            posAct = provider.size();
                        }
                        m.posEnd = posAct;
                        m.obj = message.obj;
                        m.equal = message.equal;

                        ActorRef searchActor = searchActors.get(i).start();
                        searchActor.sendOneWay(m, getContext());
                    }
                } else {    //i'm only sending one actor to improve performance
                    Message m = new Message();
                    m.operation = message.operation;
                    m.posInit = 0;
                    m.posEnd = provider.size();
                    m.obj = message.obj;
                    m.equal = message.equal;

                    MAX_ACTORS = 1;
                    ActorRef searchActor = searchActors.get(0).start();
                    searchActor.sendOneWay(m, getContext());
                }
            } else if (message.operation.equals("FINISHED")) {
                count++;
                if (count == MAX_ACTORS) {
                    if (originalActor != null) {
                        originalActor.sendOneWay(true);
                    } else {
                        future.completeWithResult(true);
                    }
                }
            } else if (message.operation.equals("SUCCESS")) {
                for (ActorRef searchActor : searchActors) {
                    searchActor.stop();
                }

                if (originalActor != null) {
                    originalActor.sendOneWay(true);
                } else {
                    future.completeWithResult(true);
                }
            }
        }
    }
}
