package pt.inevo.encontra.query.criteria;

import pt.inevo.encontra.query.criteria.exps.ExpressionFactory;
import pt.inevo.encontra.query.criteria.exps.QueryExpressions;
import pt.inevo.encontra.query.criteria.exps.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * based on org.apache.openjpa.persistence.criteria.CriteriaExpressionBuilder
 */
public class CriteriaExpressionBuilder {
    public QueryExpressions getQueryExpressions(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
        QueryExpressions exps = new QueryExpressions();

        evalDistinct(exps, factory, q);
        evalFilter(exps, factory, q);
        evalOrdering(exps, factory, q);
        exps.resultClass = q.getResultType();
 
        return exps;
    }

    protected void evalDistinct(QueryExpressions exps, ExpressionFactory factory, CriteriaQueryImpl<?> q) {
        exps.distinct = q.isDistinct() ? QueryExpressions.DISTINCT_TRUE | QueryExpressions.DISTINCT_AUTO : QueryExpressions.DISTINCT_FALSE;
     }

    protected void evalFilter(QueryExpressions exps, ExpressionFactory factory, CriteriaQueryImpl<?> q) {
        PredicateImpl where = (PredicateImpl)q.getRestrictions();

        exps.filter = where;
    }
    /**
     * Evaluates the ordering expressions by converting them to kernel values.
     * Sets the ordering fields of kernel QueryExpressions.
     *
     * @param exps kernel QueryExpressions
     * @param factory for kernel expressions
     * @param q a criteria query
     *
     */
    protected void evalOrdering(QueryExpressions exps, ExpressionFactory factory,CriteriaQueryImpl<?> q) {
        List<Order> orders = q.getOrderList();
        int ordercount = (orders == null) ? 0 : orders.size();

        exps.ordering = new Value[ordercount];
        exps.orderingClauses = new String[ordercount];
        exps.orderingAliases = new String[ordercount];
        exps.ascending = new boolean[ordercount];
        for (int i = 0; i < ordercount; i++) {
            Order order = (Order)orders.get(i);
            ExpressionImpl<?> expr = order.getExpression();
            //Value val = Expressions.toValue(expr, factory, q);
            //exps.ordering[i] = val;
            //exps.ascending[i] = order.isAscending();
        }
        
    }
}
