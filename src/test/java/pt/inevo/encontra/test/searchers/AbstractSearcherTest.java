package pt.inevo.encontra.test.searchers;

import junit.framework.TestCase;
import org.junit.Test;
import pt.inevo.encontra.benchmark.Benchmark;
import pt.inevo.encontra.benchmark.BenchmarkEntry;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.descriptors.SimpleDescriptor;
import pt.inevo.encontra.index.search.AbstractSearcher;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.Path;
import pt.inevo.encontra.query.criteria.CriteriaBuilderImpl;
import pt.inevo.encontra.test.entities.StringObject;

import java.util.logging.Logger;

/**
 * Testing the SimpleSearcher.
 *
 * @author ricardo
 */
public abstract class AbstractSearcherTest extends TestCase {

    protected AbstractSearcher<StringObject> searcher;
    protected Benchmark benchmark;
    protected Logger log = Logger.getLogger(AbstractSearcherTest.class.getName());

    public AbstractSearcherTest(String testName) {
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
        benchmark = new Benchmark("SimpleSearcherBenchmark");
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

    /*
    * Prints the results.
    */
    protected void printResults(ResultSet<StringObject> results) {
        System.out.println("Number of retrieved elements: " + results.getSize());
        for (Result<StringObject> r : results) {
            System.out.print("Retrieved element: {id:" + r.getResultObject().getId() + ", value:" + r.getResultObject().getValue() + ", ");
            System.out.println("Similarity: " + r.getScore() + "}");
        }
    }
}