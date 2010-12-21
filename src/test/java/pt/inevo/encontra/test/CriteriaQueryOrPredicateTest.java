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
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.Path;
import pt.inevo.encontra.query.QueryProcessorDefaultParallelImpl;
import pt.inevo.encontra.storage.*;

/**
 * Testing the OR predicate.
 * @author ricardo
 */
public class CriteriaQueryOrPredicateTest extends TestCase {

    private SimpleEngine<MetaTestModel> engine;
    private CriteriaBuilderImpl cb;

    public CriteriaQueryOrPredicateTest(String testName) {
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
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        Expression<Boolean> titleSimilarityClause = cb.similar(titleModel, "loremipsum");
        Expression<Boolean> contentSimilarityClause = cb.similar(contentModel, "aaaa");

        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                titleSimilarityClause,
                contentSimilarityClause)).distinct(true);

        ResultSet<MetaTestModel> results = engine.search(query);

        assertTrue(results.size() == 8);

        printResults(results);
    }

    @Test
    public void test2() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        MetaTestModel m = new MetaTestModel("ghfjsla", "ababa");
        m.setId(Long.MIN_VALUE);
        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                cb.and(
                cb.similar(titleModel, "aabbaa"),
                cb.similar(contentModel, "bbbabab")),
                cb.similar(model, m))).distinct(true);

        ResultSet<MetaTestModel> results = engine.search(query);

        assertTrue(results.size() == 8);

        printResults(results);
    }

    @Test
    public void test3() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");
        
        MetaTestModel m = new MetaTestModel("ghfjsla", "ababa");
        m.setId(Long.MIN_VALUE);

        CriteriaQuery query = cb.createQuery().where(
                cb.and(
                cb.or(
                cb.similar(titleModel, "aabbaa"),
                cb.similar(contentModel, "bbbabab")),
                cb.similar(model, m))).distinct(true);

        ResultSet<MetaTestModel> results = engine.search(query);

        assertTrue(results.size() == 8);

        printResults(results);
    }

    @Test
    public void test4() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        MetaTestModel m = new MetaTestModel("ghfjsla", "ababa");
        m.setId(Long.MIN_VALUE);

        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                cb.and(
                cb.similar(titleModel, "aabbaa"),
                cb.similar(contentModel, "bbb")),
                cb.similar(model, m))).distinct(true);

        ResultSet<MetaTestModel> results = engine.search(query);

        assertTrue(results.size() == 8);

        printResults(results);
    }

    @Test
    public void test5() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        MetaTestModel m = new MetaTestModel("ghfjsla", "ababa");
        m.setId(Long.MIN_VALUE);

        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                    cb.and(
                        cb.equal(titleModel, "aabbaa"),
                        cb.similar(contentModel, "bbb")),
                    cb.similar(model, m)));

        ResultSet<MetaTestModel> results = engine.search(query);

        assertTrue(results.size() == 8);

        printResults(results);
    }

    @Test
    public void test6() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        MetaTestModel m = new MetaTestModel("ghfjsla", "ababa");
        m.setId(Long.MIN_VALUE);

        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                    cb.and(
                        cb.not(cb.equal(titleModel, "aaa")),
                        cb.similar(contentModel, "bbb")),
                    cb.equal(model, m)));

        ResultSet<MetaTestModel> results = engine.search(query);

        assertTrue(results.size() == 7);

        printResults(results);
    }

    @Test
    public void test7() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        MetaTestModel m = new MetaTestModel("ghfjsla", "ababa");
        m.setId(Long.MIN_VALUE);

        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                    cb.and(
                        cb.not(cb.equal(titleModel, "aaa")),
                        cb.not(cb.equal(contentModel, "bba"))),
                    cb.equal(model, m)));

        ResultSet<MetaTestModel> results = engine.search(query);

        assertTrue(results.size() == 6);

        printResults(results);
    }

    @Test
    public void test8() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        MetaTestModel m = new MetaTestModel("ghfjsla", "ababa");
        m.setId(Long.MIN_VALUE);

        CriteriaQuery query = cb.createQuery().where(
                cb.not(cb.or(
                    cb.and(
                        cb.not(cb.equal(titleModel, "aaa")),
                        cb.equal(contentModel, "bba")),
                    cb.equal(model, m)))).distinct(true);

        ResultSet<MetaTestModel> results = engine.search(query);

        assertTrue(results.size() == 7);

        printResults(results);
    }

    //just print the results
    private void printResults(ResultSet<MetaTestModel> results) {
        System.out.println("Number of retrieved elements: " + results.size());
        for (Result<MetaTestModel> r : results) {
            System.out.print("Retrieved element: " + r.getResult().toString() + "\t");
            System.out.println("Similarity: " + r.getSimilarity());
        }
    }
}
