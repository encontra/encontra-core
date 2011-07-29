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
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.SimpleIndex;
import pt.inevo.encontra.index.search.SimpleSearcher;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.Path;
import pt.inevo.encontra.query.QueryProcessorDefaultImpl;
import pt.inevo.encontra.query.criteria.CriteriaBuilderImpl;
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.storage.EntityStorage;
import pt.inevo.encontra.storage.SimpleObjectStorage;
import pt.inevo.encontra.test.entities.ExampleDescriptor;
import pt.inevo.encontra.test.entities.MetaTestModel;

/**
* Smoke test: testing the creation of an engine and the search for similar
* objects in it.
* @author ricardo
*/
public class SimpleCriteriaQuerySimilarityTest extends TestCase {

    private SimpleEngine<IndexedObject> engine;
    private CriteriaBuilderImpl cb;

    public SimpleCriteriaQuerySimilarityTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //Creating a simple descriptor
        DescriptorExtractor descriptorExtractor = new SimpleDescriptorExtractor(ExampleDescriptor.class);

        //Creating the storage
        EntityStorage storage = new SimpleObjectStorage(IndexedObject.class);

        //Creating the engine and setting its properties
        engine = new SimpleEngine<IndexedObject>();
        engine.setObjectStorage(storage);
        engine.setQueryProcessor(new QueryProcessorDefaultImpl(IndexedObject.class));
        engine.setIndexedObjectFactory(new SimpleIndexedObjectFactory());
        engine.setResultProvider(new DefaultResultProvider());

        //A searcher for the "title"
        SimpleSearcher titleSearcher = new SimpleSearcher();
        titleSearcher.setDescriptorExtractor(descriptorExtractor);
        titleSearcher.setIndex(new SimpleIndex(ExampleDescriptor.class));
        titleSearcher.setResultProvider(new DefaultResultProvider());

        //A searcher for the "description"
        SimpleSearcher descriptionSearcher = new SimpleSearcher();
        descriptionSearcher.setDescriptorExtractor(descriptorExtractor);
        descriptionSearcher.setIndex(new SimpleIndex(ExampleDescriptor.class));
        descriptionSearcher.setResultProvider(new DefaultResultProvider());

        //setting the searchers
        engine.setSearcher("title", titleSearcher);
        engine.setSearcher("description", descriptionSearcher);

        //Inserting some elements into the engine (indexes)
        engine.insert(new IndexedObject(null, "title", "aaa", 0));
        engine.insert(new IndexedObject(null, "title", "aab", 0));
        engine.insert(new IndexedObject(null, "title", "bba", 0));
        engine.insert(new IndexedObject(null, "title", "abb", 0));
        engine.insert(new IndexedObject(null, "title", "abb", 0));
        engine.insert(new IndexedObject(null, "title", "baa", 0));
        engine.insert(new IndexedObject(null, "title", "aba", 0));
        engine.insert(new IndexedObject(null, "title", "bab", 0));

        engine.insert(new IndexedObject(null, "description", "aaa", 0));
        engine.insert(new IndexedObject(null, "description", "aab", 0));
        engine.insert(new IndexedObject(null, "description", "bba", 0));
        engine.insert(new IndexedObject(null, "description", "abb", 0));
        engine.insert(new IndexedObject(null, "description", "abb", 0));
        engine.insert(new IndexedObject(null, "description", "baa", 0));
        engine.insert(new IndexedObject(null, "description", "aba", 0));
        engine.insert(new IndexedObject(null, "description", "bab", 0));

        //Creating a combined query for the results
        cb = new CriteriaBuilderImpl();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void test1() {
        CriteriaQuery<IndexedObject> criteriaQuery = cb.createQuery(IndexedObject.class);

        Expression<Boolean> similar = cb.similar("title", "aba");
        CriteriaQuery query = cb.createQuery().where(similar).distinct(true).limit(8);
        ResultSet<IndexedObject> results = engine.search(query);

        //Searching in the engine for the results
        printResults(results);
    }

    @Test
    public void test2() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        Path<MetaTestModel> classPath = criteriaQuery.from(MetaTestModel.class);
        Path titlePath = classPath.get("title");

        Expression<Boolean> similar = cb.similar(titlePath, "aba");
        CriteriaQuery query = cb.createQuery().where(similar).distinct(true).limit(8);
        ResultSet<IndexedObject> results = engine.search(query);

        //Searching in the engine for the results
        printResults(results);
    }

    @Test
    public void test3() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        Path<MetaTestModel> classPath = criteriaQuery.from(MetaTestModel.class);
        Path titlePath = classPath.get("title");
        Path descriptionPath = classPath.get("description");

        Expression<Boolean> similarTitle = cb.similar(titlePath, "aba");
        Expression<Boolean> similarDescription = cb.similar(descriptionPath, "bab");
        CriteriaQuery query = cb.createQuery().where(cb.or(similarTitle, similarDescription)).distinct(true).limit(8);
        ResultSet<IndexedObject> results = engine.search(query);

        //Searching in the engine for the results
        printResults(results);
    }

    @Test
    public void test4() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        Expression<Boolean> similarTitle = cb.similar("title", "aba");
        Expression<Boolean> similarDescription = cb.similar("description", "bab");
        CriteriaQuery query = cb.createQuery().where(cb.or(similarTitle, similarDescription)).distinct(true).limit(8);
        ResultSet<IndexedObject> results = engine.search(query);

        //Searching in the engine for the results
        printResults(results);
    }

    @Test
    public void test5() {
        CriteriaQuery<IndexedObject> criteriaQuery = cb.createQuery(IndexedObject.class);

        Path<MetaTestModel> pathM = criteriaQuery.from(MetaTestModel.class);
        Path descriptionPath = pathM.get("description");

        Expression<Boolean> similarTitle = cb.similar("title", "aba");
        Expression<Boolean> similarDescription = cb.similar(descriptionPath, "bab");
        CriteriaQuery query = cb.createQuery().where(cb.or(similarTitle, similarDescription)).distinct(true).limit(8);
        ResultSet<IndexedObject> results = engine.search(query);

        //Searching in the engine for the results
        printResults(results);
    }

    @Test
    public void test6() {
        CriteriaQuery<IndexedObject> criteriaQuery = cb.createQuery(IndexedObject.class);

        Path<MetaTestModel> pathM = criteriaQuery.from(MetaTestModel.class);
        Path descriptionPath = pathM.get("description");

        Expression<Boolean> similarTitle = cb.similar("title", "aba");
        Expression<Boolean> similarDescription = cb.similar(descriptionPath, "bab");
        CriteriaQuery query = cb.createQuery().where(cb.and(similarTitle, similarDescription)).distinct(true).limit(8);
        ResultSet<IndexedObject> results = engine.search(query);

        //Searching in the engine for the results
        printResults(results);
    }

    //prints the results
    private void printResults(ResultSet<IndexedObject> results) {
        System.out.println("Number of retrieved elements: " + results.getSize());
        for (Result<IndexedObject> r : results) {
            System.out.print("Retrieved element: " + r.getResultObject().getValue() + "\t");
            System.out.println("Similarity: " + r.getScore());
        }
    }
}
