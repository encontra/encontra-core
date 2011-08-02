package pt.inevo.encontra.test.query;

import org.junit.Test;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.Path;
import pt.inevo.encontra.test.entities.MetaTestModel;

/**
* Testing the Equal expression, alone and combining it with AND and OR predicates.
* @author ricardo
*/
public class CriteriaQueryEqualTest extends AbstractCriteriaQueryTest {

    public CriteriaQueryEqualTest(String testName) {
        super(testName);
    }

    @Test
    public void test1() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);
        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);

        //Create the Query
        MetaTestModel m = new MetaTestModel("aaa", "bbb");
        CriteriaQuery query = cb.createQuery().where(cb.equal(model, m)).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //check if it returned one result
        assertTrue(results.getSize() == 1);

        printResults(results);
    }

    @Test
    public void test2() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);

        MetaTestModel m = new MetaTestModel("aaaj", "bbb");
        m.setId(Long.MIN_VALUE);
        CriteriaQuery query = cb.createQuery().where(cb.equal(model, m)).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return no results
        assertTrue(results.isEmpty());

        printResults(results);
    }

    @Test
    public void test3() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");

        CriteriaQuery query = criteriaQuery.where(cb.equal(titleModel, "aaa")).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return only one result
        assertTrue(results.getSize() == 1);

        printResults(results);
    }

    @Test
    public void test4() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        CriteriaQuery query = cb.createQuery().where(
                cb.and(
                        cb.equal(titleModel, "aaa"),
                        cb.equal(contentModel, "bbb"))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //only one result because of the and condition
        assertTrue(results.getSize() == 1);

        printResults(results);
    }

    @Test
    public void test5() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                    cb.equal(titleModel, "aaa"),
                    cb.equal(contentModel, "bba"))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return two results because of the or condition
        assertTrue(results.getSize() == 2);

        printResults(results);
    }
}