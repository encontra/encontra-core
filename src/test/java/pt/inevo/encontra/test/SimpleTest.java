package pt.inevo.encontra.test;

import junit.framework.TestCase;
import org.junit.Test;
import pt.inevo.encontra.benchmark.Benchmark;
import pt.inevo.encontra.benchmark.BenchmarkEntry;
import pt.inevo.encontra.common.DefaultResultProvider;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.descriptors.SimpleDescriptor;
import pt.inevo.encontra.index.SimpleIndex;
import pt.inevo.encontra.index.search.SimpleSearcher;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.Path;
import pt.inevo.encontra.query.criteria.CriteriaBuilderImpl;
import pt.inevo.encontra.storage.SimpleObjectStorage;
import pt.inevo.encontra.test.entities.ExampleDescriptor;
import pt.inevo.encontra.test.entities.StringObject;

import java.util.logging.Logger;

/**
 * Simple test: testing the creation of an engine and the search for similar
 * objects in it.
 *
 * @author ricardo
 */
public class SimpleTest extends TestCase {

    private SimpleSearcher<StringObject> searcher;
    private Benchmark benchmark;
    private Logger log = Logger.getLogger(SimpleTest.class.getName());

    public SimpleTest(String testName) {
        super(testName);
    }

    /**
     * An example of a Descriptor extractor
     */
    public class TestDescriptorExtractor extends DescriptorExtractor<StringObject, SimpleDescriptor> {

        public TestDescriptorExtractor(Class descriptorClass) {
            super(StringObject.class, descriptorClass);
        }

        @Override
        public SimpleDescriptor extract(StringObject object) {
            SimpleDescriptor descriptor = newDescriptor();
            descriptor.setId(object.getId());
            descriptor.setValue(object.getValue());
            return descriptor;
        }

        @Override
        protected StringObject setupIndexedObject(SimpleDescriptor descriptor, StringObject object) {
            object.setId((Long) descriptor.getId());
            object.setValue(descriptor.getValue());
            return object;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //initialize the benchmark for the test
        benchmark = new Benchmark("SimpleTestBenchmark");

        //creating a simple multi-descriptor extractor
        DescriptorExtractor descriptorExtractor = new TestDescriptorExtractor(ExampleDescriptor.class);

        //Creating the searchers - A searcher for the "title"
        searcher = new SimpleSearcher<StringObject>();
        searcher.setObjectStorage(new SimpleObjectStorage(StringObject.class));
        searcher.setDescriptorExtractor(descriptorExtractor);
        searcher.setIndex(new SimpleIndex(ExampleDescriptor.class));
        searcher.setResultProvider(new DefaultResultProvider());

        //Inserting some elements into the engine (indexes)
        for (int i = 0; i < 10000; i++)
            searcher.insert(new StringObject(Integer.toString(i)));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        //clean the necessary structures for the other test
        searcher = null;
        benchmark = null;
    }

    @Test
    public void test1() {
        //start the benchmark for the searching
        BenchmarkEntry entry = benchmark.start("test1");
        //perform the query
        ResultSet<StringObject> results = searcher.similar(new StringObject("11"), 100);
        //stop the benchmark
        entry.stop();
        //log the benchmark
        log.info(entry.toString());
        //print the results
        printResults(results);
    }

    @Test
    public void test2() {
        //start the benchmark for the searching
        BenchmarkEntry entry = benchmark.start("test2");

        //Create a query builder
        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        //Grab a path for the model StringObject
        Path<StringObject> modelPath = new Path<StringObject>(StringObject.class);
        //Create the query to search for similar objects
        CriteriaQuery query = cb.createQuery().where(cb.similar(modelPath, new StringObject("11"))).limit(100);

        //Searching in the engine for the results
        ResultSet<StringObject> results = searcher.search(query);

        //stop the benchmark
        entry.stop();
        //log the benchmark
        log.info(entry.toString());

        //print the results
        printResults(results);
    }

    //prints the results
    private void printResults(ResultSet<StringObject> results) {
        System.out.println("Number of retrieved elements: " + results.getSize());
        for (Result<StringObject> r : results) {
            System.out.print("Retrieved element: {id:" + r.getResultObject().getId() + ", value:" + r.getResultObject().getValue() + ", ");
            System.out.println("Similarity: " + r.getScore() + "}");
        }
    }
}
