package pt.inevo.encontra.test.query;

import junit.framework.TestCase;
import org.junit.Test;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.Path;
import pt.inevo.encontra.test.entities.MetaTestModel;

/**
* Testing the Equal expression, alone and combining it with AND and OR predicates.
* @author ricardo
*/
public class CriteriaQueryNotTest extends AbstractCriteriaQueryTest {

    @Test
    public void test1() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);
        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);

        //Create the Query
        MetaTestModel m = new MetaTestModel("aaa", "bbb");
        CriteriaQuery query = cb.createQuery().where(cb.not(cb.equal(model, m))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //check if it returned one result
        TestCase.assertTrue(results.getSize() == 7);

        printResults(results);
    }

    @Test
    public void test2() {

        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);

        MetaTestModel m = new MetaTestModel("aaaj", "bbb");
        CriteriaQuery query = cb.createQuery().where(cb.not(cb.similar(model, m))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return no results
        TestCase.assertTrue(results.getSize() == 7);

        printResults(results);
    }

    @Test
    public void test3() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");

        CriteriaQuery query = criteriaQuery.where(cb.not(cb.equal(titleModel, "aaa"))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return only one result
        TestCase.assertTrue(results.getSize() == 7);

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
                    cb.not(cb.equal(titleModel, "aaa")),
                    cb.equal(contentModel, "bbb"))).distinct(true).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //only one result because of the and condition
        TestCase.assertTrue(results.isEmpty());

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
                cb.and(
                    cb.not(cb.equal(titleModel, "aaa")),
                    cb.equal(contentModel, "bba"))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //only one result because of the and condition
        TestCase.assertTrue(results.getSize() == 1);

        printResults(results);
    }

    @Test
    public void test6() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                    cb.equal(titleModel, "aaa"),
                    cb.not(cb.equal(contentModel, "bba")))).distinct(true).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return two results because of the or condition
        TestCase.assertTrue(results.getSize() == 7);

        printResults(results);
    }

    @Test
    public void test7() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        //NotEqual(titleMode,"aaa") AND NotEqual(contentMode, "bba")
        CriteriaQuery query = cb.createQuery().where(
                cb.not(cb.or(
                    cb.equal(titleModel, "aaa"),
                    cb.equal(contentModel, "bba")))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return two results because of the or condition
        TestCase.assertTrue(results.getSize() == 6);

        printResults(results);
    }

    @Test
    public void test8() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        //NotEqual(titleModel, "aaa") OR NotEqual(contentModel, "bbb")
        CriteriaQuery query = cb.createQuery().where(
                cb.not(cb.and(
                    cb.equal(titleModel, "aaa"),
                    cb.equal(contentModel, "bba")))).distinct(true).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return two results because of the or condition
        TestCase.assertTrue(results.getSize() == 8);

        printResults(results);
    }

    @Test
    public void test9() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        //NotEqual(titleModel, "aaa") OR NotEqual(contentModel, "bbb")
        CriteriaQuery query = cb.createQuery().where(
                cb.and(
                    cb.not(cb.equal(titleModel, "aaa")),
                    cb.not(cb.equal(contentModel, "bba")))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return two results because of the or condition
       TestCase.assertTrue(results.getSize() == 6);

        printResults(results);
    }

    @Test
    public void test10() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        //NotEqual(titleModel, "aaa") OR NotEqual(contentModel, "bbb")
        CriteriaQuery query = cb.createQuery().where(
                cb.and(
                    cb.not(cb.equal(titleModel, "aaa")),
                    cb.or(
                        cb.equal(contentModel, "bba"))),
                        cb.similar(titleModel, "absga")).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        //should return two results because of the or condition
        TestCase.assertTrue(results.getSize() > 1);

        printResults(results);
    }

    @Test
    public void test11() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");

        //NotEqual(titleModel, "aaa") OR NotEqual(contentModel, "bbb")
        CriteriaQuery query = cb.createQuery().where(
                cb.not(cb.not(cb.equal(titleModel, "aaa")))).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        TestCase.assertTrue(results.getSize() == 1);

        printResults(results);
    }
}
