package pt.inevo.encontra.test;

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
import pt.inevo.encontra.storage.*;
import pt.inevo.encontra.test.entities.ExampleDescriptor;

/**
 * Smoke test: testing the creation of an engine and the search for similar
 * objects in it.
 * @author ricardo
 */
public class CriteriaQuerySimilarityTest extends TestCase {

    private SimpleEngine<MetaTestModel> engine;
    private CriteriaBuilderImpl cb;

    public CriteriaQuerySimilarityTest(String testName) {
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

        // TODO must remove this setID call to object
        MetaTestModel testObject = new MetaTestModel("ghaksdfd", "aaaa");
        testObject.setId(Long.MIN_VALUE);

        Expression<Boolean> similar = cb.similar(model, testObject);
        CriteriaQuery query = cb.createQuery().where(similar);
        ResultSet<MetaTestModel> results = engine.search(query);

        //Searching in the engine for the results
        printResults(results);
    }

    @Test
    public void test2() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        Expression<Boolean> titleSimilarityClause = cb.similar(titleModel, "ghak");
        Expression<Boolean> contentSimilarityClause = cb.similar(contentModel, "aaaa");

        //Create the Query
        CriteriaQuery query = cb.createQuery().where(
                cb.and(titleSimilarityClause, contentSimilarityClause));

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        printResults(results);
    }

    //prints the results
    private void printResults(ResultSet<MetaTestModel> results) {
        System.out.println("Number of retrieved elements: " + results.size());
        for (Result<MetaTestModel> r : results) {
            System.out.print("Retrieved element: " + r.getResult().toString() + "\t");
            System.out.println("Similarity: " + r.getSimilarity());
        }
    }
}
