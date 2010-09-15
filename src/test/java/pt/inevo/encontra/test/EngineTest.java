package pt.inevo.encontra.test;


import pt.inevo.encontra.descriptors.Descriptor;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.descriptors.SimpleDescriptor;
import pt.inevo.encontra.descriptors.SimpleDescriptorExtractor;
import pt.inevo.encontra.engine.SimpleEngine;
import pt.inevo.encontra.engine.Engine;
import junit.framework.TestCase;
import pt.inevo.encontra.engine.SimpleIndexedObjectFactory;
import pt.inevo.encontra.index.*;
import pt.inevo.encontra.index.annotation.Indexed;
import pt.inevo.encontra.index.search.SimpleCombinedSearcher;
import pt.inevo.encontra.index.search.SimpleSearcher;
import pt.inevo.encontra.query.*;
import pt.inevo.encontra.storage.*;


/**
 * Smoke test: testing the creation of a simple engine, two indexes and the
 * execution of two random queries (testing also the combination of the queries).
 * @author ricardo
 */
public class EngineTest extends TestCase {


    public static class TestDescriptor extends SimpleDescriptor {

        public TestDescriptor(){
            int i=0;
        }
        
        @Override
        public double getDistance(Descriptor other) {
            return getLevenshteinDistance(getValue(),(String)other.getValue());
        }

        public int getLevenshteinDistance (String s, String t) {
            if (s == null || t == null) {
                throw new IllegalArgumentException("Strings must not be null");
            }

            int n = s.length(); // length of s
            int m = t.length(); // length of t

            if (n == 0) {
                return m;
            } else if (m == 0) {
                return n;
            }

            int p[] = new int[n+1]; //'previous' cost array, horizontally
            int d[] = new int[n+1]; // cost array, horizontally
            int _d[]; //placeholder to assist in swapping p and d

            // indexes into strings s and t
            int i; // iterates through s
            int j; // iterates through t

            char t_j; // jth character of t

            int cost; // cost

            for (i = 0; i<=n; i++) {
                p[i] = i;
            }

            for (j = 1; j<=m; j++) {
                t_j = t.charAt(j-1);
                d[0] = j;

                for (i=1; i<=n; i++) {
                    cost = s.charAt(i-1)==t_j ? 0 : 1;
                    // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                    d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);
                }

                // copy current distance counts to 'previous row' distance counts
                _d = p;
                p = d;
                d = _d;
            }

            // our last action in the above loop was to switch d and p, so p now
            // actually has the most recent cost counts
            return p[n];
        }
    }

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
        DescriptorExtractor descriptorExtractor=new SimpleDescriptorExtractor(TestDescriptor.class);

        EntityStorage storage=new SimpleObjectStorage(TestModel.class);
        System.out.println("Creating the Retrieval Engine...");

        Engine<TestModel> e = new SimpleEngine<TestModel>();
        e.setObjectStorage(storage);
        e.setIndexedObjectFactory(new SimpleIndexedObjectFactory());

        System.out.println("Registering the two indexes in the Retrieval Engine");
        SimpleCombinedSearcher searcher=new SimpleCombinedSearcher();

        SimpleSearcher titleSearcher=new SimpleSearcher();
        titleSearcher.setDescriptorExtractor(descriptorExtractor);
        titleSearcher.setIndex(new SimpleIndex(TestDescriptor.class));


        SimpleSearcher contentSearcher=new SimpleSearcher();
        contentSearcher.setDescriptorExtractor(descriptorExtractor);
        contentSearcher.setIndex(new SimpleIndex(TestDescriptor.class));

        searcher.addSearcher("title",titleSearcher);
        searcher.addSearcher("content",contentSearcher);

        e.setSearcher(searcher);


        System.out.println("Loading some objects to the test indexes");
        e.insert(new TestModel("aaa","bbb"));
        e.insert(new TestModel("aab","bba"));
        e.insert(new TestModel("aba","bab"));
        e.insert(new TestModel("abb","baa"));
        e.insert(new TestModel("baa","abb"));
        e.insert(new TestModel("bab","aba"));
        e.insert(new TestModel("bba","aab"));
        e.insert(new TestModel("bbb","aaa"));

        System.out.println("Making some random queries and searching in the engine:");
        System.out.println("Creating two random queries...");
        Query randomQuery = new RandomQuery();
        Query anotherRandomQuery = new RandomQuery();
        System.out.println("Creating a knn query...");

        Query knnQuery = new KnnQuery(new IndexedObject(null,"aaa"), 8);

        System.out.println("Searching for elements in the engine...");
        QueryBuilder qb=new QueryBuilder();
        // select * from randomquery inner join anotherRandomQuery inner join randomQuery
        //qb.semijoin( randomQuery ,anotherRandomQuery, randomQuery ) <-> innerjoin <-> intersect
        //qb.antijoin( randomQuery ,anotherRandomQuery, randomQuery )  <-> !innerjoin <->

        ResultSet<TestModel> results = e.search(knnQuery); //new Query[]{randomQuery, anotherRandomQuery, });
        System.out.println("Number of retrieved elements: " + results.size());
        for ( Result<TestModel> r : results) {
            System.out.print("Retrieved element: " + r.getResult().toString() + "\t");
            System.out.println("Similarity: " + r.getSimilarity());
        }
    }
}