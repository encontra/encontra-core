package pt.inevo.encontra.test.query;

import org.junit.BeforeClass;
import org.junit.Test;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.Path;
import pt.inevo.encontra.query.QueryProcessorDefaultParallelImpl;
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.test.entities.MetaTestModel;

/**
* Testing the parallel QueryProcessor.
* TODO make this work
* @author ricardo
*/
public class CriteriaQueryParallelTest extends AbstractCriteriaQueryTest {

    @BeforeClass
    public static void setUp() throws Exception {
        AbstractCriteriaQueryTest.setUp();
        engine.setQueryProcessor(new QueryProcessorDefaultParallelImpl());
    }

    @Test
    public void test1() {
        System.out.println("Test1");
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path title = model.get("title");
        Path content = model.get("content");

        Expression<Boolean> similar = cb.similar(title, "aaa");
        Expression<Boolean> similarContent = cb.similar(content, "bbb");
        CriteriaQuery query = cb.createQuery().where(cb.and(similar, similarContent)).distinct(true);
        ResultSet<MetaTestModel> results = engine.search(query);

        //Searching in the engine for the results
        printResults(results);
    }

    @Test
    public void test2() {
        System.out.println("Test2");
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        Expression<Boolean> titleSimilarityClause = cb.similar(titleModel, "ghak");
        Expression<Boolean> contentSimilarityClause = cb.similar(contentModel, "aaaa");

        //Create the Query
        CriteriaQuery query = cb.createQuery().where(
                cb.and(titleSimilarityClause, contentSimilarityClause)).distinct(true);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        printResults(results);
    }

    @Test
    public void test3() {
        System.out.println("Test3");
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        Expression<Boolean> titleSimilarityClause = cb.similar(titleModel, "ghak");
        Expression<Boolean> contentSimilarityClause = cb.similar(contentModel, "aaaa");

        //Create the Query
        CriteriaQuery query = cb.createQuery().where(
                cb.and(titleSimilarityClause, contentSimilarityClause));

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        printResults(results);
    }

    @Test
    public void test4() {
        System.out.println("Test4");
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        Expression<Boolean> titleEqualClause = cb.equal(titleModel, "aaa");
        Expression<Boolean> contentEqualClause = cb.equal(contentModel, "bbb");

        //Create the Query
        CriteriaQuery query = cb.createQuery().where(
                cb.and(titleEqualClause, contentEqualClause));

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        printResults(results);
    }

    @Test
    public void test5() {
        System.out.println("Test5");
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");

        Expression<Boolean> titleEqualClause = cb.equal(titleModel, "aaa");

        //Create the Query
        CriteriaQuery query = cb.createQuery().where(titleEqualClause);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        printResults(results);
    }

    @Test
    public void test6() {
        System.out.println("Test6");
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);

        MetaTestModel testObject = new MetaTestModel("aaa", "bbb");
        testObject.setId(Long.MIN_VALUE);

        Expression<Boolean> clause = cb.similar(model, testObject);

        //Create the Query
        CriteriaQuery query = cb.createQuery().where(clause);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        printResults(results);
    }

    @Test
    public void test7() {
        System.out.println("Test7");
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);

        MetaTestModel testObject = new MetaTestModel("aaa", "bbb");
        testObject.setId(Long.MIN_VALUE);

        Expression<Boolean> clause = cb.equal(model, testObject);

        //Create the Query
        CriteriaQuery query = cb.createQuery().where(clause).distinct(true);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        printResults(results);
    }
}
