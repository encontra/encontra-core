package pt.inevo.encontra.query.criteria;

import pt.inevo.encontra.query.Query;
import pt.inevo.encontra.query.criteria.exps.*;

/**
 * Based on openjpa-persistence/src/main/java/org/apache/openjpa/persistence/criteria/CriteriaBuilderImpl.java
 */
public class CriteriaBuilderImpl implements ExpressionParser{
    /*
    public QueryExpressions eval(Object parsed, Query query,ExpressionFactory factory) {
        CriteriaQueryImpl<?> c = (CriteriaQueryImpl<?>) parsed;
        return c.getQueryExpressions(factory);
    }

    public Value[] eval(String[] vals, Query query,ExpressionFactory factory) {
        return null;
    }

    public Object parse(String ql, ExpressionStoreQuery query) {
        throw new AbstractMethodError();
    }

    public void populate(Object parsed, ExpressionStoreQuery query) {
        CriteriaQueryImpl<?> c = (CriteriaQueryImpl<?>) parsed;
        query.invalidateCompilation();
        query.getContext().setCandidateType(c.getRoot().getJavaType(), true);
        query.setQuery(parsed);
    }          */

    /**
     *  Create a Criteria query object with the specified result type.
     *  @param resultClass  type of the query result
     *  @return query object
     */
    public <T> CriteriaQuery<T> createQuery(Class<T> resultClass) {
        return new CriteriaQueryImpl<T>(resultClass);
    }

    public Predicate and(Predicate... restrictions) {
        return new And(restrictions);
    }

    public Predicate and(Expression<Boolean> x, Expression<Boolean> y) {
        return new And(x,y);
    }

    public CriteriaQuery<Object> createQuery() {
        return new CriteriaQueryImpl<Object>(Object.class);
    }

    public Predicate not(Expression<Boolean> restriction) {
        return ((Predicate)restriction).not();
    }

    public Predicate or(Predicate... restrictions) {
        return new Or(restrictions);
    }

    public Predicate or(Expression<Boolean> x, Expression<Boolean> y) {
        return new Or(x,y);
    }

    public Order asc(Expression<?> x) {
        return new Order(x, true);
    }

    public Order desc(Expression<?> x) {
        return new Order(x, false);
    }

    public Predicate equal(Expression<?> x, Expression<?> y) {
        if (y == null)
            return new IsNull((ExpressionImpl<?> )x);
        return new Equal(x, y);
    }

    public Predicate equal(Expression<?> x, Object y) {
        if (y == null)
            return new IsNull((ExpressionImpl<?> )x);
        return new Equal(x, y);
    }

    public Predicate similar(Expression<?> x, Object y) {
        return new Similar(x, y);
    }

    public Predicate similar(Object x, Object y) {
        return new Similar(x, y);
    }

}