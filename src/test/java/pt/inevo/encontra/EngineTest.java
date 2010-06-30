package pt.inevo.encontra;

import junit.framework.TestCase;
import pt.inevo.encontra.index.Index;
import pt.inevo.encontra.index.Result;
import pt.inevo.encontra.index.ResultSet;
import pt.inevo.encontra.query.KnnQuery;
import pt.inevo.encontra.query.Query;
import pt.inevo.encontra.query.RandomQuery;

/**
 * Smoke test: testing the creation of a simple engine, two indexes and the
 * execution of two random queries (testing also the combination of the queries).
 * @author ricardo
 */
public class EngineTest extends TestCase {
    
    public EngineTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMain() {
        System.out.println("Creating the Retrieval Engine...");
        Engine e = new SimpleEngine();

        System.out.println("Creating two indexes");
        Index textualIndex = new LinearIndex();
        Index otherTextualIndex = new LinearIndex();

        System.out.println("Loading some objects to the test indexes");
        for (int i = 0; i < 10; i++) {
            textualIndex.insertObject(new StringObject("StringObject" + i));
            otherTextualIndex.insertObject(new StringObject("StringObject" + i));
        }

        System.out.println("Registering the two indexes in the Retrieval Engine");
        e.registerIndex(textualIndex);
        e.registerIndex(otherTextualIndex);

        System.out.println("Making some random queries and searching in the engine:");
        System.out.println("Creating two random queries...");
        Query randomQuery = new RandomQuery();
        Query anotherRandomQuery = new RandomQuery();
        System.out.println("Creating a knn query...");
        Query knnQuery = new KnnQuery(new StringObject("StringObject1"), 5);

        System.out.println("Searching for elements in the engine...");
        ResultSet results = e.search(new Query[]{randomQuery, anotherRandomQuery, knnQuery});
        System.out.println("Number of retrieved elements: " + results.getSize());
        while (results.hasNext()) {

            Result r = results.getNext();
            System.out.print("Retrieved element: " + r.getResultObject().toString() + "\t");
            System.out.println("Similarity: " + r.getSimilarity());
        }
    }
}