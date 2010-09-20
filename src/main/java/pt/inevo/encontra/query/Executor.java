package pt.inevo.encontra.query;

import java.util.Map;

/**
 * An executor provides a uniform interface to the mechanism for executing
 * either an in-memory or datastore query. In the common case, the
 * {@link #executeQuery} method will be called before other methods,
 * though this is not guaranteed.
 *
 * @author Marc Prud'hommeaux
 */
public interface Executor {
//    /**
//     * A query result range.
//     */
//    public static class Range {
//        public long start = 0L;
//        public long end = Long.MAX_VALUE;
//        public boolean lrs = false;
//
//        public Range() {
//        }
//
//        public Range(long start, long end) {
//            this.start = start;
//            this.end = end;
//        }
//    }
//    /**
//     * Return the result of executing this query with the given parameter
//     * values. If this query is a projection and this executor does not
//     * pack results itself, each element of the returned result object
//     * provider should be an object array containing the projection values.
//     *
//     * @param lrs true if the query result should be treated as a
//     * large result set, assuming the query is not an
//     * aggregate and does not have grouping
//     * @see #isPacking
//     */
//    public ResultObjectProvider executeQuery(StoreQuery q, Object[] params,
//                                             Range range);
//
//
//
//
//
//
//
//    /**
//     * Mutate the given range to set any range information stored in
//     * the query string and/or parameters.
//     */
//    public void getRange(StoreQuery q, Object[] params, Range range);
//
//    /**
//     * Extract the value of the <code>orderIndex</code>th ordering
//     * expression in {@link Query#getOrderingClauses} from the
//     * given result object. The result object will be an object from
//     * the result object provider returned from {@link #executeQuery}.
//     * This method is used when several result lists have to be merged
//     * in memory. If this exeuctor's parent query supports executors on
//     * abstract or interface classes, this method will not be used.
//     *
//     * @see StoreQuery#supportsAbstractExecutors
//     */
//    public Object getOrderingValue(StoreQuery q, Object[] params,
//                                   Object resultObject, int orderIndex);
//
//    /**
//     * Return the ordering direction for all ordering clauses, or empty
//     * array if none.
//     */
//    public boolean[] getAscending(StoreQuery q);
//
//    /**
//     * Return true if this executor packs projections into the result
//     * class itself. Executors for query languages that allow projections
//     * without result clauses must return true and perform the result
//     * packing themselves.
//     */
//    public boolean isPacking(StoreQuery q);
//
//    /**
//     * If this is not a projection but the candidate results are placed
//     * into a result class with an alias, return that alias.
//     */
//    public String getAlias(StoreQuery q);
//
//    /**
//     * Return the alias for each projection element, or empty array
//     * if not a projection.
//     */
//    public String[] getProjectionAliases(StoreQuery q);
//
//    /**
//     * Return the expected types of the projections used by this query,
//     * or an empty array if not a projection.
//     */
//    public Class<?>[] getProjectionTypes(StoreQuery q);
//
//    /**
//     * Return an array of all persistent classes used in this query, or
//     * empty array if unknown.
//     */
//    public ClassMetaData[] getAccessPathMetaDatas(StoreQuery q);
//
//    /**
//     * Returns the operation this executor is meant to execute.
//     *
//     * @see QueryOperations
//     */
//    public int getOperation(StoreQuery q);
//
//    /**
//     * Return true if the compiled query is an aggregate.
//     */
//    public boolean isAggregate(StoreQuery q);
//
//    public boolean isDistinct(StoreQuery q);
//
//    /**
//     * Whether the compiled query has grouping.
//     */
//    public boolean hasGrouping(StoreQuery q);
//
//    /**
//     * Return a map of parameter names to types. The returned
//     * {@link java.util.Map#entrySet}'s {@link java.util.Iterator} must return values in the
//     * order in which they were declared or used.
//     */
//    public OrderedMap<Object, Class<?>> getOrderedParameterTypes(StoreQuery q);
//
//    /**
//     * Return a map of parameter names to types. The returned
//     * {@link java.util.Map#entrySet}'s {@link java.util.Iterator} must return values in the
//     * order in which they were declared or used.<br>
//     *
//     * <B>Warning</B>: Deprecated. Use {@linkplain #getOrderedParameterTypes(StoreQuery)} instead.
//     */
//    @Deprecated
//    public LinkedMap getParameterTypes(StoreQuery q);
//
//    /**
//     * Return an array from the given user parameter values.
//     * The array ordering is same as what this executor expects for its
//     * executeXXX() methods as its Object[] parameters.
//     * If the given userParams is null or empty return an empty array
//     * rather than null.
//     *
//     * @return array with parameter values ordered in the same way as this
//     * receiver's executeXXX() method expects.
//     *
//     * @since 2.0.0
//     */
//    public Object[] toParameterArray(StoreQuery q, Map<?,?> userParams);
//
//    /**
//     * Returns the result class, if any.
//     */
//    public Class<?> getResultClass(StoreQuery q);
//
//    public ResultShape<?> getResultShape(StoreQuery q);
//
//    /**
//     * Return a map of {@link FieldMetaData} to update
//     * {@link Constant}s, in cases where this query is for a bulk update.
//     */
//    public Map<FieldMetaData,Value> getUpdates (StoreQuery q);
//
//    /**
//     * Return the parsed query expressions for our candidate types.
//     * The expressions are available only after query has been parsed.
//     *
//     * @since 2.0.0
//     */
//    public QueryExpressions[] getQueryExpressions();
}
