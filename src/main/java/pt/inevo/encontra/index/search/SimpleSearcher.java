package pt.inevo.encontra.index.search;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.dispatch.CompletableFuture;
import akka.dispatch.Future;
import java.util.ArrayList;
import java.util.List;
import pt.inevo.encontra.descriptors.Descriptor;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.descriptors.DescriptorList;
import pt.inevo.encontra.index.EntryProvider;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.Result;
import pt.inevo.encontra.index.ResultSet;
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
 * Simple searcher
 */
public class SimpleSearcher<O extends IEntity> extends AbstractSearcher<O> {

    protected DescriptorExtractor extractor;
    protected DescriptorList resultList;

    public SimpleSearcher() {
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
        ResultSet<IEntry> results = new ResultSet<IEntry>();

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

        results.sort();
        return getResultObjects(results);
    }

    protected ResultSet<IEntry> performKnnQuery(Descriptor d, int maxHits) {

        ResultSet resultSet = new ResultSet<Descriptor>();
        resultList = new DescriptorList(maxHits, d);

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
            for (Descriptor descr : resultList.getDescriptors()) {
                Result<Descriptor> result = new Result<Descriptor>(descr);
                result.setSimilarity(descr.getDistance(d)); // TODO - This is distance not similarity!!!
                resultSet.add(result);
            }

            resultSet.normalizeScores();
            resultSet.invertScores(); // This is a distance (dissimilarity) and we need similarity
        }
        return resultSet;
    }

    protected ResultSet<IEntry> performEqualQuery(Descriptor d, boolean equal) {

        ResultSet resultSet = new ResultSet<Descriptor>();
        if (!equal)
            resultList = new DescriptorList(index.getEntryProvider().size(), d);
        else resultList = new DescriptorList(1, d);

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
            for (Descriptor descr : resultList.getDescriptors()) {
                Result<Descriptor> result = new Result<Descriptor>(descr);
                result.setSimilarity(descr.getDistance(d)); // TODO - This is distance not similarity!!!
                resultSet.add(result);
            }

            resultSet.normalizeScores();
            resultSet.invertScores(); // This is a distance (dissimilarity) and we need similarity
        }
        return resultSet;
    }

    @Override
    protected Result<O> getResultObject(Result<IEntry> indexEntryresult) {
        return new Result<O>((O) getDescriptorExtractor().getIndexedObject((Descriptor) indexEntryresult.getResult()));
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
                    if (!resultList.contains(p)) {
                        //insert only if it doesn't already exists
                        if (!resultList.addDescriptor(p)) {
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
                        resultList.addDescriptor(desc);
                        break;
                    } else if (!message.equal && distance != 0) {
                        resultList.addDescriptor(desc);
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