package pt.inevo.encontra.test.query;

import org.junit.Test;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.Path;
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.test.AbstractCriteriaQueryTest;
import pt.inevo.encontra.test.entities.MetaTestModel;

/**
* Main Similarity Test. CriteriaQuery similarity tests.
* @author ricardo
*/
public class CriteriaQuerySimilarityTest extends AbstractCriteriaQueryTest {

    public CriteriaQuerySimilarityTest(String testName) {
        super(testName);
    }

    @Test
    public void test1() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);
        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);

        MetaTestModel testObject = new MetaTestModel("ghaksdfd", "aaaa");

        Expression<Boolean> similar = cb.similar(model, testObject);
        CriteriaQuery query = cb.createQuery().where(similar).distinct(true).limit(8);
        ResultSet<MetaTestModel> results = engine.search(query);

        //Searching in the engine for the results
        printResults(results);
    }

    @Test
    public void test2() {
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class);

        //Create the Model/Attributes Path
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);
        Path<String> titleModel = model.get("title");
        Path<String> contentModel = model.get("content");

        Expression<Boolean> titleSimilarityClause = cb.similar(titleModel, "ghak");
        Expression<Boolean> contentSimilarityClause = cb.similar(contentModel, "aaaa");

        //Create the Query
        CriteriaQuery query = cb.createQuery().where(
                cb.and(titleSimilarityClause, contentSimilarityClause)).distinct(true).limit(8);

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
        Path<String> contentModel = model.get("content");

        Expression<Boolean> titleSimilarityClause = cb.similar(titleModel, "ghak");
        Expression<Boolean> contentSimilarityClause = cb.similar(contentModel, "aaaa");

        //Create the Query
        CriteriaQuery query = cb.createQuery().where(
                cb.and(titleSimilarityClause, contentSimilarityClause)).distinct(true).limit(8);

        //Searching in the engine for the results
        ResultSet<MetaTestModel> results = engine.search(query);

        printResults(results);
    }
}
