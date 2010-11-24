package pt.inevo.encontra.test;

import pt.inevo.encontra.test.entities.MetaTestModel;
import pt.inevo.encontra.query.Path;
import pt.inevo.encontra.query.CriteriaQuery;
import junit.framework.TestCase;
import pt.inevo.encontra.query.criteria.*;
import pt.inevo.encontra.query.criteria.exps.Similar;

public class CriteriaQueryTest extends TestCase {

    private static class Visitor extends ExpressionVisitor.AbstractVisitor {

        @Override
        public void enter(Expression expr) {
            if (expr instanceof Similar) {
                Similar s = (Similar) expr;
                assert (true);
            }// else if (expr instanceof)
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCriteriaQuery() {
        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        CriteriaQuery<MetaTestModel> criteriaQuery = cb.createQuery(MetaTestModel.class); //

        // Path acts as a meta model using reflection
        Path<MetaTestModel> model = criteriaQuery.from(MetaTestModel.class);

        Expression<Boolean> where1 = cb.similar(model, new MetaTestModel("Teste", "aaaa"));

        Expression<Boolean> where2 = cb.and(
                cb.equal(model.get("title"), "Teste"),
                cb.similar(model.get("content"), "aaaa"));

        //criteriaQuery=criteriaQuery.distinct(true).where(where);
        //criteriaQuery.where(where2);

        where1.acceptVisit(new Visitor());

        where2.acceptVisit(new Visitor());
    }
}
