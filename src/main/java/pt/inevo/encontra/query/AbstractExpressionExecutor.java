package pt.inevo.encontra.query;

import pt.inevo.encontra.query.criteria.exps.QueryExpressions;

import java.util.*;

/**
 * Provides support for queries that hold query information
 * in a {@link QueryExpressions} instance.
 *
 * @author Marc Prud'hommeaux
 */
public abstract class AbstractExpressionExecutor
        extends AbstractExecutor
        implements Executor {
//
//    /**
//     * Return the query expressions for one candidate type, or die if none.
//     */
//    private QueryExpressions assertQueryExpression() {
//        QueryExpressions[] exp = getQueryExpressions();
//        if (exp == null || exp.length < 1)
//            throw new InvalidStateException(_loc.get("no-expressions"));
//
//        return exp[0];
//    }
//
//    /**
//     * Throw proper exception if given value is a collection/map/array.
//     */
//    protected void assertNotContainer(Value val, StoreQuery q) {
//        // variables represent container elements, not the container itself
//        if (val.isVariable())
//            return;
//
//        Class<?> type;
//        if (val instanceof Path) {
//            FieldMetaData fmd = ((Path) val).last();
//            type = (fmd == null) ? val.getType() : fmd.getDeclaredType();
//        } else
//            type = val.getType();
//
//        switch (JavaTypes.getTypeCode(type)) {
//            case JavaTypes.ARRAY:
//            case JavaTypes.COLLECTION:
//            case JavaTypes.MAP:
//                throw new UserException(_loc.get("container-projection",
//                        q.getContext().getQueryString()));
//        }
//    }
//
//    public final void validate(StoreQuery q) {
//        QueryExpressions exps = assertQueryExpression();
//        ValidateGroupingExpressionVisitor.validate(q.getContext(), exps);
//    }
//
//    public void getRange(StoreQuery q, Object[] params, Range range) {
//        QueryExpressions exps = assertQueryExpression();
//        if (exps.range.length == 0)
//            return;
//
//        if (exps.range.length == 2
//                && exps.range[0] instanceof Constant
//                && exps.range[1] instanceof Constant) {
//            try {
//                range.start = ((Number) ((Constant) exps.range[0]).
//                        getValue(params)).longValue();
//                range.end = ((Number) ((Constant) exps.range[1]).
//                        getValue(params)).longValue();
//                return;
//            } catch (ClassCastException cce) {
//                // fall through to exception below
//            } catch (NullPointerException npe) {
//                // fall through to exception below
//            }
//        }
//        throw new UserException(_loc.get("only-range-constants",
//                q.getContext().getQueryString()));
//    }
//
//    public final Class<?> getResultClass(StoreQuery q) {
//        return assertQueryExpression().resultClass;
//    }
//
//    public final ResultShape<?> getResultShape(StoreQuery q) {
//        return assertQueryExpression().shape;
//    }
//
//    public final boolean[] getAscending(StoreQuery q) {
//        return assertQueryExpression().ascending;
//    }
//
//    public final String getAlias(StoreQuery q) {
//        return assertQueryExpression().alias;
//    }
//
//    public final String[] getProjectionAliases(StoreQuery q) {
//        return assertQueryExpression().projectionAliases;
//    }
//
//    public Class<?>[] getProjectionTypes(StoreQuery q) {
//        return null;
//    }
//
//    public final int getOperation(StoreQuery q) {
//        return assertQueryExpression().operation;
//    }
//
//    public final boolean isAggregate(StoreQuery q) {
//        return assertQueryExpression().isAggregate();
//    }
//
//    public final boolean isDistinct(StoreQuery q) {
//        return assertQueryExpression().isDistinct();
//    }
//
//    public final boolean hasGrouping(StoreQuery q) {
//        return assertQueryExpression().grouping.length > 0;
//    }
//
//    public final OrderedMap<Object,Class<?>> getOrderedParameterTypes(StoreQuery q) {
//        return assertQueryExpression().parameterTypes;
//    }
//
//    /**
//     * Creates a Object[] from the values of the given user parameters.
//     */
//    public Object[] toParameterArray(StoreQuery q, Map<?,?> userParams) {
//        if (userParams == null || userParams.isEmpty())
//            return StoreQuery.EMPTY_OBJECTS;
//
//        OrderedMap<?,Class<?>> paramTypes = getOrderedParameterTypes(q);
//        Object[] arr = new Object[userParams.size()];
//        int base = positionalParameterBase(userParams.keySet());
//        for (Object key : paramTypes.keySet()) {
//            int idx = (key instanceof Integer)
//                    ? ((Integer)key).intValue() - base
//                    : paramTypes.indexOf(key);
//            if (idx >= arr.length || idx < 0)
//                throw new UserException(_loc.get("gap-query-param",
//                        new Object[]{q.getContext().getQueryString(), key,
//                                userParams.size(), userParams}));
//            Object value = userParams.get(key);
//            validateParameterValue(key, value, (Class)paramTypes.get(key));
//            arr[idx] = value;
//        }
//        return arr;
//    }
//
//    /**
//     * Return the base (generally 0 or 1) to use for positional parameters.
//     */
//    private static int positionalParameterBase(Collection params) {
//        int low = Integer.MAX_VALUE;
//        Object obj;
//        int val;
//        for (Iterator itr = params.iterator(); itr.hasNext();) {
//            obj = itr.next();
//            if (!(obj instanceof Number))
//                return 0; // use 0 base when params are mixed types
//
//            val = ((Number) obj).intValue();
//            if (val == 0)
//                return val;
//            if (val < low)
//                low = val;
//        }
//        return low;
//    }
//
//    private static void validateParameterValue(Object key, Object value,
//                                               Class expected) {
//        if (expected == null)
//            return;
//
//        if (value == null) {
//            if (expected.isPrimitive())
//                throw new UserException(_loc.get("null-primitive-param",
//                        key, expected));
//        } else {
//            Class actual = value.getClass();
//            boolean strict = true;
//            if (!Filters.canConvert(actual, expected, strict))
//                throw new UserException(_loc.get("param-value-mismatch",
//                        new Object[]{key, expected, value, actual}));
//        }
//    }
//
//    public final Map getUpdates(StoreQuery q) {
//        return assertQueryExpression().updates;
//    }
//
//    public final ClassMetaData[] getAccessPathMetaDatas(StoreQuery q) {
//        QueryExpressions[] exps = getQueryExpressions();
//        if (exps.length == 1)
//            return exps[0].accessPath;
//
//        List<ClassMetaData> metas = null;
//        for (int i = 0; i < exps.length; i++)
//            metas = Filters.addAccessPathMetaDatas(metas,
//                    exps[i].accessPath);
//        if (metas == null)
//            return StoreQuery.EMPTY_METAS;
//        return (ClassMetaData[]) metas.toArray
//                (new ClassMetaData[metas.size()]);
//    }
//
//    public boolean isPacking(StoreQuery q) {
//        return false;
//    }
//
//    /**
//     * Throws an exception if select or having clauses contain
//     * non-aggregate, non-grouped paths.
//     */
//    private static class ValidateGroupingExpressionVisitor
//            extends AbstractExpressionVisitor {
//
//        private final QueryContext _ctx;
//        private boolean _grouping = false;
//        private Set _grouped = null;
//        private Value _agg = null;
//
//        /**
//         * Throw proper exception if query does not meet validation.
//         */
//        public static void validate(QueryContext ctx,
//                                    QueryExpressions exps) {
//            if (exps.grouping.length == 0)
//                return;
//
//            ValidateGroupingExpressionVisitor visitor =
//                    new ValidateGroupingExpressionVisitor(ctx);
//            visitor._grouping = true;
//            for (int i = 0; i < exps.grouping.length; i++)
//                exps.grouping[i].acceptVisit(visitor);
//            visitor._grouping = false;
//            if (exps.having != null)
//                exps.having.acceptVisit(visitor);
//            for (int i = 0; i < exps.projections.length; i++)
//                exps.projections[i].acceptVisit(visitor);
//        }
//
//        public ValidateGroupingExpressionVisitor(QueryContext ctx) {
//            _ctx = ctx;
//        }
//
//        public void enter(Value val) {
//            if (_grouping) {
//                if (val instanceof Path) {
//                    if (_grouped == null)
//                        _grouped = new HashSet();
//                    _grouped.add(val);
//                }
//            } else if (_agg == null) {
//                if (val.isAggregate())
//                    _agg = val;
//                else if (val instanceof Path
//                        && (_grouped == null || !_grouped.contains(val))) {
//                    throw new UserException(_loc.get("bad-grouping",
//                            _ctx.getCandidateType(), _ctx.getQueryString()));
//                }
//            }
//        }
//
//        public void exit(Value val) {
//            if (val == _agg)
//                _agg = null;
//        }
//    }
}
