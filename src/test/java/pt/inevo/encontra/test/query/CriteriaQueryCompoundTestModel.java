package pt.inevo.encontra.test.query;

import junit.framework.TestCase;
import org.junit.Test;
import pt.inevo.encontra.common.DefaultResultProvider;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.descriptors.SimpleDescriptorExtractor;
import pt.inevo.encontra.engine.SimpleEngine;
import pt.inevo.encontra.engine.SimpleIndexedObjectFactory;
import pt.inevo.encontra.index.SimpleIndex;
import pt.inevo.encontra.index.search.SimpleSearcher;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.Path;
import pt.inevo.encontra.query.QueryProcessorDefaultImpl;
import pt.inevo.encontra.query.criteria.CriteriaBuilderImpl;
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.storage.EntityStorage;
import pt.inevo.encontra.storage.SimpleObjectStorage;
import pt.inevo.encontra.test.entities.CompoundMetaTestModel;
import pt.inevo.encontra.test.entities.ExampleDescriptor;
import pt.inevo.encontra.test.entities.MetaTestModel;

//import pt.inevo.encontra.query.QueryProcessorDefaultParallelImpl;

/**
* Smoke test: testing the creation of an engine and the search for similar
* objects in it.
* @author ricardo
*/
public class CriteriaQueryCompoundTestModel extends TestCase {

    private SimpleEngine<CompoundMetaTestModel> engine;
    private CriteriaBuilderImpl cb;

    //MetaTestModelQueryProcessor - for testing purposes
//    class MetaTestModelQueryProcessor<E extends IEntity> extends QueryProcessorDefaultImpl<E> {
//
//        @Override
//        public boolean insert(E object) {
//            if (object instanceof IndexedObject) {
//                IndexedObject obj = (IndexedObject) object;
//
//                //is it a compound object
//                if (obj.getValue() instanceof IEntity) {
//                    //first set the ID
//                    IEntity entity = (IEntity) obj.getValue();
//                    entity.setId(obj.getId());
//                    processBean(entity);
//
//                } else {    //its not a compound object
//                    insertObject(object);
//                }
//
//            } else {
//                processBean(object);
//            }
//
//            return true;
//        }
//
//        private void processBean(IEntity entity) {
//            try {
//                List<IndexedObject> indexedObjects = indexedObjectFactory.processBean(entity);
//                for (IndexedObject obj : indexedObjects) {
//                    insertObject((E) obj);
//                }
//            } catch (IndexingException e) {
//                System.out.println("[Error] Exception: " + e.getMessage());
//            }
//        }
//    }

//    //MetaTestModelSearcher - for testing purposes
//    class MetaTestModelSearcher<O extends IEntity> extends AbstractSearcher<O> {
//
//        @Override
//        public boolean insert(O object) {
//            return queryProcessor.insert(object);
//        }
//
//        @Override
//        public boolean remove(O object) {
//            return queryProcessor.remove(object);
//        }
//
//        @Override
//        public ResultSet search(Query query) {
//            return queryProcessor.search(query);
//        }
//
//        @Override
//        protected Result getResultObject(Result<IEntry> entryResult) {
//            return new Result<O>((O) storage.get(entryResult.getResultObject().getId()));
//        }
//    }

    public CriteriaQueryCompoundTestModel(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //Creating a simple descriptor
        DescriptorExtractor descriptorExtractor = new SimpleDescriptorExtractor(ExampleDescriptor.class);

        //Creating the storage
        EntityStorage storage = new SimpleObjectStorage(CompoundMetaTestModel.class);

        //Creating the engine and setting its properties
        engine = new SimpleEngine<CompoundMetaTestModel>();
        engine.setObjectStorage(storage);
        engine.setQueryProcessor(new QueryProcessorDefaultImpl());
        engine.setIndexedObjectFactory(new SimpleIndexedObjectFactory());
        engine.setResultProvider(new DefaultResultProvider());

        //Creating the searchers - searchers for native fields (not complex here)
        SimpleSearcher nameSeacher = new SimpleSearcher();
        nameSeacher.setDescriptorExtractor(descriptorExtractor);
        nameSeacher.setIndex(new SimpleIndex(ExampleDescriptor.class));
        nameSeacher.setResultProvider(new DefaultResultProvider());

        SimpleSearcher<MetaTestModel> modelTestSearcher = new SimpleSearcher<MetaTestModel>();
        modelTestSearcher.setQueryProcessor(new QueryProcessorDefaultImpl());
        modelTestSearcher.setObjectStorage(storage);
        modelTestSearcher.setIndexedObjectFactory(new SimpleIndexedObjectFactory());
        modelTestSearcher.setResultProvider(new DefaultResultProvider());

        //A performQuery for the "title"
        SimpleSearcher titleSearcher = new SimpleSearcher();
        titleSearcher.setDescriptorExtractor(descriptorExtractor);
        titleSearcher.setIndex(new SimpleIndex(ExampleDescriptor.class));
        titleSearcher.setResultProvider(new DefaultResultProvider());

        //A performQuery for the "content"
        SimpleSearcher contentSearcher = new SimpleSearcher();
        contentSearcher.setDescriptorExtractor(descriptorExtractor);
        contentSearcher.setIndex(new SimpleIndex(ExampleDescriptor.class));
        contentSearcher.setResultProvider(new DefaultResultProvider());

        //setting the searchers
        modelTestSearcher.setSearcher("title", titleSearcher);
        modelTestSearcher.setSearcher("content", contentSearcher);

        engine.setSearcher("name", nameSeacher);
        engine.setSearcher("testModel", modelTestSearcher);

        //Inserting some elements into the engine
        engine.insert(new CompoundMetaTestModel("name1", new MetaTestModel("aaa", "bbb")));
        engine.insert(new CompoundMetaTestModel("name2", new MetaTestModel("aab", "bba")));
        engine.insert(new CompoundMetaTestModel("name3", new MetaTestModel("aba", "bab")));
        engine.insert(new CompoundMetaTestModel("name4", new MetaTestModel("abb", "baa")));
        engine.insert(new CompoundMetaTestModel("name5", new MetaTestModel("baa", "abb")));
        engine.insert(new CompoundMetaTestModel("name6", new MetaTestModel("bab", "aba")));
        engine.insert(new CompoundMetaTestModel("name7", new MetaTestModel("bba", "aab")));
        engine.insert(new CompoundMetaTestModel("name8", new MetaTestModel("bbb", "aaa")));

        //Creating a combined query for the results
        cb = new CriteriaBuilderImpl();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void test1() {
        System.out.println("Test1");

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        Path<CompoundMetaTestModel> model = criteriaQuery.from(CompoundMetaTestModel.class);
        Path metaModelTitlePath = model.get("testModel");

        MetaTestModel t = new MetaTestModel("aaa", "bbb");
        t.setId(Long.MAX_VALUE);
        Expression<Boolean> similarityClause = cb.similar(metaModelTitlePath, t);

        //Create the Query;
        CriteriaQuery query = cb.createQuery().where(similarityClause);

        //Searching in the engine for the results
        ResultSet<CompoundMetaTestModel> results = engine.search(query);
        printResults(results);
    }

    @Test
    public void test2() {
        System.out.println("Test2");

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        Path<CompoundMetaTestModel> model = criteriaQuery.from(CompoundMetaTestModel.class);
        Path namePath = model.get("name");
        Expression<Boolean> similarityClause = cb.similar(namePath, "name2");

        //Create the Query;
        CriteriaQuery query = cb.createQuery().where(similarityClause);

        //Searching in the engine for the results
        ResultSet<CompoundMetaTestModel> results = engine.search(query);
        printResults(results);
    }

    @Test
    public void test3() {
        System.out.println("Test3");

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);
        Path<CompoundMetaTestModel> model = criteriaQuery.from(CompoundMetaTestModel.class);

        Path<String> metaModelTitlePath = model.get("testModel").get("title");
        Expression<Boolean> similarityClause = cb.similar(metaModelTitlePath, "bab");

        //Create the Query;
        CriteriaQuery query = cb.createQuery().where(similarityClause);

        //Searching in the engine for the results
        ResultSet<CompoundMetaTestModel> results = engine.search(query);
        printResults(results);
    }

    @Test
    public void test4() {
        System.out.println("Test4");

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        Path<CompoundMetaTestModel> model = criteriaQuery.from(CompoundMetaTestModel.class);

        // TODO must remove the setId call from here, because this is not corrent
        MetaTestModel metaModelObject = new MetaTestModel("aaa", "bbb");
        metaModelObject.setId(Long.MAX_VALUE);

        // TODO must remove the setId call from here, because this is not corrent
        CompoundMetaTestModel testObject = new CompoundMetaTestModel("name1", metaModelObject);
        testObject.setId(Long.MIN_VALUE);

        Expression<Boolean> similarityClause = cb.similar(model, testObject);

        //Create the Query;
        CriteriaQuery query = cb.createQuery().where(similarityClause);

        //Searching in the engine for the results
        ResultSet<CompoundMetaTestModel> results = engine.search(query);
        printResults(results);
    }

    @Test
    public void test5() {
        System.out.println("Test5");

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);
        Path<CompoundMetaTestModel> model = criteriaQuery.from(CompoundMetaTestModel.class);

        Path<String> metaModelTitlePath = model.get("testModel").get("title");
        Expression<Boolean> equalClause = cb.equal(metaModelTitlePath, "bab");

        //Create the Query;
        CriteriaQuery query = cb.createQuery().where(equalClause).limit(10);

        //Searching in the engine for the results
        ResultSet<CompoundMetaTestModel> results = engine.search(query);

        assertTrue(results.getSize() == 1);

        printResults(results);
    }

    @Test
    public void test6() {
        System.out.println("Test6");

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);
        Path<CompoundMetaTestModel> model = criteriaQuery.from(CompoundMetaTestModel.class);

        Path<String> metaModelTitlePath = model.get("testModel").get("title");
        Path<String> contentModelPath = model.get("testModel").get("content");
        Expression<Boolean> equalClause = cb.not(cb.equal(metaModelTitlePath, "bab"));
        Expression<Boolean> contentClause = cb.similar(contentModelPath, "aab");

        //Create the Query;
        CriteriaQuery query = cb.createQuery().where(cb.and(equalClause, contentClause)).distinct(true).limit(8);

        //Searching in the engine for the results
        ResultSet<CompoundMetaTestModel> results = engine.search(query);

        assertTrue(results.getSize() == 7);

        printResults(results);
    }

    @Test
    public void test7() {
        System.out.println("Test7");

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);
        Path<CompoundMetaTestModel> model = criteriaQuery.from(CompoundMetaTestModel.class);

        Path<String> metaModelTitlePath = model.get("testModel").get("title");
        Path<String> contentModelPath = model.get("testModel").get("content");

        Expression<Boolean> equalClause = cb.not(cb.equal(metaModelTitlePath, "bab"));
        Expression<Boolean> contentClause = cb.similar(contentModelPath, "aab");

        //Create the Query;
        CriteriaQuery query = cb.createQuery().where(cb.and(equalClause, contentClause)).distinct(true).limit(8);

        //Searching in the engine for the results
        ResultSet<CompoundMetaTestModel> results = engine.search(query);

        assertTrue(results.getSize() == 7);

        printResults(results);
    }

    private void printResults(ResultSet<CompoundMetaTestModel> results) {
        System.out.println("Number of retrieved elements: " + results.getSize());
        for (Result<CompoundMetaTestModel> r : results) {
            System.out.print("Retrieved element: " + r.getResultObject().toString() + "\t");
            System.out.println("Similarity: " + r.getScore());
        }
    }
}
