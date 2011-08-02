package pt.inevo.encontra.test.query;

import junit.framework.TestCase;
import org.junit.Test;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.Path;
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.test.entities.MetaTestModel;

/**
 * Testing the OR predicate.
 *
 * @author ricardo
 */
public class CriteriaQueryOrPredicateTest extends AbstractCriteriaQueryTest {

    @Test
    public void test1() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        Expression<Boolean> titleSimilarityClause = cb.similar(titleModel, "loremipsum");
        Expression<Boolean> contentSimilarityClause = cb.similar(contentModel, "aaaa");

        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                        titleSimilarityClause,
                        contentSimilarityClause)).distinct(true).limit(8);

        ResultSet<MetaTestModel> results = engine.search(query);

        TestCase.assertTrue(results.getSize() == 8);

        printResults(results);
    }

    @Test
    public void test2() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        MetaTestModel m = new MetaTestModel("ghfjsla", "ababa");
        m.setId(Long.MIN_VALUE);
        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                        cb.and(
                                cb.similar(titleModel, "aabbaa"),
                                cb.similar(contentModel, "bbbabab")),
                        cb.similar(model, m))).distinct(true).limit(8);

        ResultSet<MetaTestModel> results = engine.search(query);

        TestCase.assertTrue(results.getSize() == 8);

        printResults(results);
    }

    @Test
    public void test3() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        MetaTestModel m = new MetaTestModel("ghfjsla", "ababa");
        m.setId(Long.MIN_VALUE);

        CriteriaQuery query = cb.createQuery().where(
                cb.and(
                        cb.or(
                                cb.similar(titleModel, "aabbaa"),
                                cb.similar(contentModel, "bbbabab")),
                        cb.similar(model, m))).distinct(true).limit(8);

        ResultSet<MetaTestModel> results = engine.search(query);

        TestCase.assertTrue(results.getSize() == 8);

        printResults(results);
    }

    @Test
    public void test4() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        MetaTestModel m = new MetaTestModel("ghfjsla", "ababa");
        m.setId(Long.MIN_VALUE);

        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                        cb.and(
                                cb.similar(titleModel, "aabbaa"),
                                cb.similar(contentModel, "bbb")),
                        cb.similar(model, m))).distinct(true).limit(8);

        ResultSet<MetaTestModel> results = engine.search(query);

        TestCase.assertTrue(results.getSize() == 8);

        printResults(results);
    }

    @Test
    public void test5() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        MetaTestModel m = new MetaTestModel("ghfjsla", "ababa");
        m.setId(Long.MIN_VALUE);

        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                        cb.and(
                                cb.equal(titleModel, "aabbaa"),
                                cb.similar(contentModel, "bbb")),
                        cb.similar(model, m))).limit(8);

        ResultSet<MetaTestModel> results = engine.search(query);

        TestCase.assertTrue(results.getSize() == 8);

        printResults(results);
    }

    @Test
    public void test6() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        MetaTestModel m = new MetaTestModel("ghfjsla", "ababa");
        m.setId(Long.MIN_VALUE);

        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                        cb.and(
                                cb.not(cb.equal(titleModel, "aaa")),
                                cb.similar(contentModel, "bbb")),
                        cb.equal(model, m))).limit(8);

        ResultSet<MetaTestModel> results = engine.search(query);

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

        MetaTestModel m = new MetaTestModel("ghfjsla", "ababa");
        m.setId(Long.MIN_VALUE);

        CriteriaQuery query = cb.createQuery().where(
                cb.or(
                        cb.and(
                                cb.not(cb.equal(titleModel, "aaa")),
                                cb.not(cb.equal(contentModel, "bba"))),
                        cb.equal(model, m))).limit(8);

        ResultSet<MetaTestModel> results = engine.search(query);

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

        MetaTestModel m = new MetaTestModel("ghfjsla", "ababa");
        m.setId(Long.MIN_VALUE);

        CriteriaQuery query = cb.createQuery().where(
                cb.not(cb.or(
                        cb.and(
                                cb.not(cb.equal(titleModel, "aaa")),
                                cb.equal(contentModel, "bba")),
                        cb.equal(model, m)))).distinct(true).limit(8);

        ResultSet<MetaTestModel> results = engine.search(query);

        TestCase.assertTrue(results.getSize() == 7);

        printResults(results);
    }
}
