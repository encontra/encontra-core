package pt.inevo.encontra.test;

import pt.inevo.encontra.test.entities.ExampleDescriptor;
import pt.inevo.encontra.test.entities.MetaTestModel;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.descriptors.SimpleDescriptorExtractor;
import junit.framework.TestCase;
import org.junit.Test;
import pt.inevo.encontra.engine.SimpleEngine;
import pt.inevo.encontra.query.QueryProcessorDefaultImpl;
import pt.inevo.encontra.engine.SimpleIndexedObjectFactory;
import pt.inevo.encontra.index.*;
import pt.inevo.encontra.index.search.SimpleSearcher;
import pt.inevo.encontra.query.criteria.CriteriaBuilderImpl;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.Path;
import pt.inevo.encontra.query.QueryProcessorDefaultParallelImpl;
import pt.inevo.encontra.storage.*;

/**
 * Testing the Equal expression, alone and combining it with AND and OR predicates.
 * @author ricardo
 */
public class CriteriaQueryEqualTest extends TestCase {

    private SimpleEngine<MetaTestModel> engine;
    private CriteriaBuilderImpl cb;

    public CriteriaQueryEqualTest(String testName) {
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
//        engine.setQueryProcessor(new QueryProcessorDefaultParallelImpl());
        engine.getQueryProcessor().setIndexedObjectFactory(new SimpleIndexedObjectFactory());

        //Creating the searchers
        //A searcher for the "title"
        SimpleSearcher titleSearcher = new SimpleSearcher();
        titleSearcher.setDescriptorExtractor(descriptorExtractor);
        titleSearcher.setIndex(new SimpleIndex(ExampleDescriptor.class));

        //A searcher for the "content"
        SimpleSearcher contentSearcher = new SimpleSearcher();
        contentSearcher.setDescriptorExtractor(descriptorExtractor);
        contentSearcher.setIndex(new SimpleIndex(ExampleDescriptor.class));

        //setting the searchers
        engine.getQueryProcessor().setSearcher("title", titleSearcher);
        engine.getQueryProcessor().setSearcher("content", contentSearcher);

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
        // query 1
        MetaTestModel m = new MetaTestModel("aaa", "bbb");
        m.setId(Long.MIN_VALUE);
        CriteriaQuery query = cb.createQuery().where(cb.equal(model, m));

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //check if it returned one result
        assertTrue(results.size() == 1);

        printResults(results);
    }

    @Test
    public void test2() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);

        MetaTestModel m = new MetaTestModel("aaaj", "bbb");
        m.setId(Long.MIN_VALUE);
        CriteriaQuery query = cb.createQuery().where(cb.equal(model, m));

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return no results
        assertTrue(results.isEmpty());

        printResults(results);
    }

    @Test
    public void test3() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");

        CriteriaQuery query = criteriaQuery.where(cb.equal(titleModel, "aaa"));

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return only one result
        assertTrue(results.size() == 1);

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
                    cb.equal(titleModel, "aaa"),
                    cb.equal(contentModel, "bbb")));

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //only one result because of the and condition
        assertTrue(results.size() == 1);

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
                cb.or(
                    cb.equal(titleModel, "aaa"),
                    cb.equal(contentModel, "bba")));

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return two results because of the or condition
        assertTrue(results.size() == 2);

        printResults(results);
    }

    //just print the results to the standard output
    private void printResults(ResultSet<MetaTestModel> results) {
        System.out.println("Number of retrieved elements: " + results.size());
        for (Result<MetaTestModel> r : results) {
            System.out.print("Retrieved element: " + r.getResult().toString() + "\t");
            System.out.println("Similarity: " + r.getSimilarity());
        }
    }
}