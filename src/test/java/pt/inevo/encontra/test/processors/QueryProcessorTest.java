package pt.inevo.encontra.test.processors;

import org.junit.Test;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.Path;
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.test.query.AbstractCriteriaQueryTest;
import pt.inevo.encontra.test.entities.MetaTestModel;

/**
* Testing the Query Processor (legacy test).
* @author ricardo
*/
public class QueryProcessorTest extends AbstractCriteriaQueryTest {

    public QueryProcessorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        for (int i = 0; i < 1000; i++) {
            engine.insert(new MetaTestModel("aia", "bi"));
        }
    }

    @Test
    public void test1() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        Expression<Boolean> titleSimilarityClause = cb.similar(titleModel, "ghak");
        Expression<Boolean> contentSimilarityClause = cb.similar(contentModel, "aaa");

        //Create the Query
        CriteriaQuery query = cb.createQuery().where(
                cb.and(titleSimilarityClause, contentSimilarityClause)).distinct(true).limit(30);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        printResults(results);
    }

    @Test
    public void test2() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);

        MetaTestModel m = new MetaTestModel("aab", "ghak");

        //Create the Query
        CriteriaQuery query = cb.createQuery().
                where(cb.similar(model, m)).distinct(true).limit(20);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        printResults(results);
    }

    @Test
    public void test3() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");

        MetaTestModel m = new MetaTestModel("aab", "ghak");

        //Create the Query
        CriteriaQuery query = cb.createQuery().where(
                cb.and(
                        cb.similar(model, m),
                        cb.equal(titleModel, "aaa"))).distinct(true).limit(20);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        printResults(results);
    }

    @Test
    public void test4() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");

        //Create the Query
        CriteriaQuery query = cb.createQuery().where(cb.equal(titleModel, "aaa")).distinct(true).limit(20);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        printResults(results);
    }
}
