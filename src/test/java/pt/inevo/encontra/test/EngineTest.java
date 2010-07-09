package pt.inevo.encontra.test;

import pt.inevo.encontra.StringObject;
import pt.inevo.encontra.engine.SimpleEngine;
import pt.inevo.encontra.engine.Engine;
import junit.framework.TestCase;
import pt.inevo.encontra.index.*;
import pt.inevo.encontra.query.*;
import pt.inevo.encontra.storage.IEntry;
import pt.inevo.encontra.storage.ObjectStorage;
import pt.inevo.encontra.storage.SimpleObjectStorage;
import pt.inevo.encontra.storage.StorableObject;

import java.io.Serializable;

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
        Engine<StringObject> e = new SimpleEngine<StringObject>();
        e.setQueryCombiner(new SimpleQueryCombiner());
        e.setObjectStorage(new SimpleObjectStorage(StringObject.class));

        System.out.println("Creating two indexes");
        Index textualIndex = new SimpleIndex();
        Index otherTextualIndex = new SimpleIndex();

        IndexEntryFactory<StringObject, IndexEntry> indexEntryFactory=new SimpleIndexEntryFactory<StringObject>();

        System.out.println("Registering the two indexes in the Retrieval Engine");
        e.registerIndex(textualIndex,indexEntryFactory);
        e.registerIndex(otherTextualIndex,indexEntryFactory);

        System.out.println("Loading some objects to the test indexes");
        for (int i = 0; i < 10; i++) {
            e.insert(new StringObject("StringObject" + i));
        }

        System.out.println("Making some random queries and searching in the engine:");
        System.out.println("Creating two random queries...");
        Query randomQuery = new RandomQuery();
        Query anotherRandomQuery = new RandomQuery();
        System.out.println("Creating a knn query...");

        Query knnQuery = new KnnQuery(new StringObject("StringObject1"), 5);

        System.out.println("Searching for elements in the engine...");
        ResultSet<StringObject> results = e.search(new Query[]{randomQuery, anotherRandomQuery, knnQuery});
        System.out.println("Number of retrieved elements: " + results.size());
        for ( Result<StringObject> r : results) {
            System.out.print("Retrieved element: " + r.toString() + "\t");
            System.out.println("Similarity: " + r.getSimilarity());
        }
    }
}