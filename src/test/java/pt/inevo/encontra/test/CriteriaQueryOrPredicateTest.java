package pt.inevo.encontra.test;

import pt.inevo.encontra.descriptors.Descriptor;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.descriptors.SimpleDescriptor;
import pt.inevo.encontra.descriptors.SimpleDescriptorExtractor;
import junit.framework.TestCase;
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

/**
 * Smoke test: testing the creation of an engine and the search for similar
 * objects in it.
 * @author ricardo
 */
public class CriteriaQueryOrPredicateTest extends TestCase {

    //Example of a simple descriptor for test purposes
    public static class TestDescriptor extends SimpleDescriptor {

        public TestDescriptor() {
            int i = 0;
        }

        @Override
        public double getDistance(Descriptor other) {
            return getLevenshteinDistance(getValue(), (String) other.getValue());
        }

        public int getLevenshteinDistance(String s, String t) {
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

            int p[] = new int[n + 1]; //'previous' cost array, horizontally
            int d[] = new int[n + 1]; // cost array, horizontally
            int _d[]; //placeholder to assist in swapping p and d

            // indexes into strings s and t
            int i; // iterates through s
            int j; // iterates through t

            char t_j; // jth character of t

            int cost; // cost

            for (i = 0; i <= n; i++) {
                p[i] = i;
            }

            for (j = 1; j <= m; j++) {
                t_j = t.charAt(j - 1);
                d[0] = j;

                for (i = 1; i <= n; i++) {
                    cost = s.charAt(i - 1) == t_j ? 0 : 1;
                    // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                    d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
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

    public CriteriaQueryOrPredicateTest(String testName) {
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
        //Creating a simple descriptor
        DescriptorExtractor descriptorExtractor = new SimpleDescriptorExtractor(TestDescriptor.class);

        //Creating the storage
        EntityStorage storage = new SimpleObjectStorage(MetaTestModel.class);

        //Creating the engine and setting its properties
        SimpleEngine<MetaTestModel> engine = new SimpleEngine<MetaTestModel>();
        engine.setObjectStorage(storage);
        engine.setQueryProcessor(new QueryProcessorDefaultImpl());
        engine.getQueryProcessor().setIndexedObjectFactory(new SimpleIndexedObjectFactory());

        //Creating the searchers
        //A searcher for the "title"
        SimpleSearcher titleSearcher = new SimpleSearcher();
        titleSearcher.setDescriptorExtractor(descriptorExtractor);
        titleSearcher.setIndex(new SimpleIndex(TestDescriptor.class));

        //A searcher for the "content"
        SimpleSearcher contentSearcher = new SimpleSearcher();
        contentSearcher.setDescriptorExtractor(descriptorExtractor);
        contentSearcher.setIndex(new SimpleIndex(TestDescriptor.class));

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
        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        Expression<Boolean> titleSimilarityClause = cb.similar(titleModel, "ghakçjflçs");
        Expression<Boolean> contentSimilarityClause = cb.similar(contentModel, "aaaa");

        //Create the Query
        // query 1
//        CriteriaQuery query = cb.createQuery().where(
//                cb.or(titleSimilarityClause, contentSimilarityClause));

        //Other queries

        // query 2
        MetaTestModel m = new MetaTestModel("ghfjslça", "ababa");
        m.setId(Long.MIN_VALUE);
        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                    cb.and(
                        cb.similar(titleModel, "aabbaa"),
                        cb.similar(contentModel, "bbbabab")),
                    cb.similar(model, m))
                );

        // query 3
//        CriteriaQuery query = cb.createQuery().where(
//                cb.and(
//                    cb.or(
//                        cb.similar(titleModel, "aabbaa"),
//                        cb.similar(contentModel, "bbbabab")),
//                    cb.similar(model, m))
//                );

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        System.out.println("Number of retrieved elements: " + results.size());
        for (Result<MetaTestModel> r : results) {
            System.out.print("Retrieved element: " + r.getResult().toString() + "\t");
            System.out.println("Similarity: " + r.getSimilarity());
        }
    }
}