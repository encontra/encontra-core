package pt.inevo.encontra.test;

import junit.framework.TestCase;
import org.junit.Test;
import pt.inevo.encontra.common.DefaultResultProvider;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.descriptors.Descriptor;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.descriptors.MultiDescriptorExtractor;
import pt.inevo.encontra.descriptors.SimpleDescriptorExtractor;
import pt.inevo.encontra.engine.SimpleEngine;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.SimpleIndex;
import pt.inevo.encontra.index.search.SimpleSearcher;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.Path;
import pt.inevo.encontra.query.criteria.CriteriaBuilderImpl;
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.test.entities.ExampleDescriptor;
import pt.inevo.encontra.test.entities.MetaTestModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
* Smoke test: testing the creation of an engine and the search for similar
* objects in it.
* @author ricardo
*/
public class SmokeTest extends TestCase {

    private SimpleEngine<MetaTestModel> engine;
    private CriteriaBuilderImpl cb;

    public SmokeTest(String testName) {
        super(testName);
    }

    class SimpleMultiDescriptorExtractor extends MultiDescriptorExtractor<IndexedObject, Descriptor> {

        private DescriptorExtractor descriptorExtractor = new SimpleDescriptorExtractor(ExampleDescriptor.class);

        SimpleMultiDescriptorExtractor() {
            super();
        }

        @Override
        protected List<Descriptor> extractDescriptors(IndexedObject object) {

            Descriptor d = descriptorExtractor.extract(object);
            List<Descriptor> descs = new ArrayList<Descriptor>();
            descs.add(d);
            descs.add(d);
            return descs;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //Creating a simple descriptor
        DescriptorExtractor descriptorExtractor = new SimpleDescriptorExtractor(ExampleDescriptor.class);

        //Creating the engine and setting its properties
        engine = new SimpleEngine<MetaTestModel>();

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
        for (int i = 0; i < 10000 ; i++)
            engine.insert(new MetaTestModel(i+"", i+""));

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
        MetaTestModel testObject = new MetaTestModel("11", "111");
        testObject.setId(Long.MIN_VALUE);

        Expression<Boolean> similar = cb.similar(model, testObject);
        CriteriaQuery query = cb.createQuery().where(similar).distinct(true).limit(20);

        //Searching in the engine for the results
        long timeBefore = Calendar.getInstance().getTimeInMillis();
        ResultSet<MetaTestModel> results = engine.search(query);
        long timeAfter = Calendar.getInstance().getTimeInMillis();
        System.out.println("Search took: " + (timeAfter - timeBefore));
        printResults(results);
    }

    @Test
    public void test2() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        Expression<Boolean> titleSimilarityClause = cb.similar(titleModel, "211");
        Expression<Boolean> contentSimilarityClause = cb.similar(contentModel, "101");

        //Create the Query
        CriteriaQuery query = cb.createQuery().where(
                cb.and(titleSimilarityClause, contentSimilarityClause)).distinct(true).limit(20);

        //Searching in the engine for the results
        long timeBefore = Calendar.getInstance().getTimeInMillis();
        ResultSet<MetaTestModel> results = engine.search(query);
        long timeAfter = Calendar.getInstance().getTimeInMillis();
        System.out.println("Search took: " + (timeAfter - timeBefore));
        printResults(results);
    }

    @Test
    public void test3() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        Expression<Boolean> titleSimilarityClause = cb.similar(titleModel, "101");
        Expression<Boolean> contentSimilarityClause = cb.similar(contentModel, "11");

        //Create the Query
        CriteriaQuery query = cb.createQuery().where(
                cb.and(titleSimilarityClause, contentSimilarityClause)).distinct(true).limit(20);

        //Searching in the engine for the results
        long timeBefore = Calendar.getInstance().getTimeInMillis();
        ResultSet<MetaTestModel> results = engine.search(query);
        long timeAfter = Calendar.getInstance().getTimeInMillis();
        System.out.println("Search took: " + (timeAfter - timeBefore));
        printResults(results);
    }

    //prints the results
    private void printResults(ResultSet<MetaTestModel> results) {
        System.out.println("Number of retrieved elements: " + results.getSize());
        for (Result<MetaTestModel> r : results) {
            System.out.print("Retrieved element: " + r.getResultObject().toString() + "\t");
            System.out.println("Similarity: " + r.getScore());
        }
    }
}
