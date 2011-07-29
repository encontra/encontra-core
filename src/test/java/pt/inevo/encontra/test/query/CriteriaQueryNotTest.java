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
import pt.inevo.encontra.storage.EntityStorage;
import pt.inevo.encontra.storage.SimpleObjectStorage;
import pt.inevo.encontra.test.entities.ExampleDescriptor;
import pt.inevo.encontra.test.entities.MetaTestModel;

/**
* Testing the Equal expression, alone and combining it with AND and OR predicates.
* @author ricardo
*/
public class CriteriaQueryNotTest extends TestCase {

    private SimpleEngine<MetaTestModel> engine;
    private CriteriaBuilderImpl cb;

    public CriteriaQueryNotTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //Creating a simple descriptor
        DescriptorExtractor descriptorExtractor = new SimpleDescriptorExtractor(ExampleDescriptor.class);

        //Creating the storage
        EntityStorage storage = new SimpleObjectStorage(MetaTestModel.class);

        //Creating the engine and setting its properties
        engine = new SimpleEngine<MetaTestModel>();
        engine.setObjectStorage(storage);
        engine.setQueryProcessor(new QueryProcessorDefaultImpl());
        engine.setIndexedObjectFactory(new SimpleIndexedObjectFactory());
        engine.setResultProvider(new DefaultResultProvider());

        //Creating the searchers
        //A searcher for the "title"
        SimpleSearcher titleSearcher = new SimpleSearcher();
        titleSearcher.setDescriptorExtractor(descriptorExtractor);
        titleSearcher.setIndex(new SimpleIndex(ExampleDescriptor.class));
        titleSearcher.setResultProvider(new DefaultResultProvider());

        //A searcher for the "content"
        SimpleSearcher contentSearcher = new SimpleSearcher();
        contentSearcher.setDescriptorExtractor(descriptorExtractor);
        contentSearcher.setIndex(new SimpleIndex(ExampleDescriptor.class));
        contentSearcher.setResultProvider(new DefaultResultProvider());

        //setting the searchers
        engine.setSearcher("title", titleSearcher);
        engine.setSearcher("content", contentSearcher);

        //Inserting some elements into the engine (indexes)
        engine.insert(new MetaTestModel("aaa", "bbb"));
        engine.insert(new MetaTestModel("aab", "bba"));
        engine.insert(new MetaTestModel("aba", "bab"));
        engine.insert(new MetaTestModel("abb", "baa"));
        engine.insert(new MetaTestModel("baa", "abb"));
        engine.insert(new MetaTestModel("bab", "aba"));
        engine.insert(new MetaTestModel("bba", "aab"));
        engine.insert(new MetaTestModel("bbb", "aaa"));

        //Creating a combined query for the results
        cb = new CriteriaBuilderImpl();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void test1() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);
        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);

        //Create the Query
        MetaTestModel m = new MetaTestModel("aaa", "bbb");
        m.setId(Long.MIN_VALUE);
        CriteriaQuery query = cb.createQuery().where(cb.not(cb.equal(model, m))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //check if it returned one result
        assertTrue(results.getSize() == 7);

        printResults(results);
    }

    @Test
    public void test2() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);

        MetaTestModel m = new MetaTestModel("aaaj", "bbb");
        m.setId(Long.MIN_VALUE);
        CriteriaQuery query = cb.createQuery().where(cb.not(cb.similar(model, m))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return no results
        assertTrue(results.getSize() == 7);

        printResults(results);
    }

    @Test
    public void test3() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");

        CriteriaQuery query = criteriaQuery.where(cb.not(cb.equal(titleModel, "aaa"))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return only one result
        assertTrue(results.getSize() == 7);

        printResults(results);
    }

    @Test
    public void test4() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        CriteriaQuery query = cb.createQuery().where(
                cb.and(
                    cb.not(cb.equal(titleModel, "aaa")),
                    cb.equal(contentModel, "bbb"))).distinct(true).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //only one result because of the and condition
        assertTrue(results.isEmpty());

        printResults(results);
    }

    @Test
    public void test5() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        CriteriaQuery query = cb.createQuery().where(
                cb.and(
                    cb.not(cb.equal(titleModel, "aaa")),
                    cb.equal(contentModel, "bba"))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //only one result because of the and condition
        assertTrue(results.getSize() == 1);

        printResults(results);
    }

    @Test
    public void test6() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                    cb.equal(titleModel, "aaa"),
                    cb.not(cb.equal(contentModel, "bba")))).distinct(true).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return two results because of the or condition
        assertTrue(results.getSize() == 7);

        printResults(results);
    }

    @Test
    public void test7() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        //NotEqual(titleMode,"aaa") AND NotEqual(contentMode, "bba")
        CriteriaQuery query = cb.createQuery().where(
                cb.not(cb.or(
                    cb.equal(titleModel, "aaa"),
                    cb.equal(contentModel, "bba")))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return two results because of the or condition
        assertTrue(results.getSize() == 6);

        printResults(results);
    }

    @Test
    public void test8() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        //NotEqual(titleModel, "aaa") OR NotEqual(contentModel, "bbb")
        CriteriaQuery query = cb.createQuery().where(
                cb.not(cb.and(
                    cb.equal(titleModel, "aaa"),
                    cb.equal(contentModel, "bba")))).distinct(true).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return two results because of the or condition
        assertTrue(results.getSize() == 8);

        printResults(results);
    }

    @Test
    public void test9() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        //NotEqual(titleModel, "aaa") OR NotEqual(contentModel, "bbb")
        CriteriaQuery query = cb.createQuery().where(
                cb.and(
                    cb.not(cb.equal(titleModel, "aaa")),
                    cb.not(cb.equal(contentModel, "bba")))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return two results because of the or condition
        assertTrue(results.getSize() == 6);

        printResults(results);
    }

    @Test
    public void test10() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        //NotEqual(titleModel, "aaa") OR NotEqual(contentModel, "bbb")
        CriteriaQuery query = cb.createQuery().where(
                cb.and(
                    cb.not(cb.equal(titleModel, "aaa")),
                    cb.or(
                        cb.equal(contentModel, "bba"))),
                        cb.similar(titleModel, "absga")).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return two results because of the or condition
        assertTrue(results.getSize() > 1);

        printResults(results);
    }

    @Test
    public void test11() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");

        //NotEqual(titleModel, "aaa") OR NotEqual(contentModel, "bbb")
        CriteriaQuery query = cb.createQuery().where(
                cb.not(cb.not(cb.equal(titleModel, "aaa")))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        assertTrue(results.getSize() == 1);

        printResults(results);
    }

    //just print the results to the standard output
    private void printResults(ResultSet<MetaTestModel> results) {
        System.out.println("Number of retrieved elements: " + results.getSize());
        for (Result<MetaTestModel> r : results) {
            System.out.print("Retrieved element: " + r.getResultObject().toString() + "\t");
            System.out.println("Similarity: " + r.getScore());
        }
    }
}
