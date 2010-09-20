package pt.inevo.encontra.query;

import java.util.Map;

/**
 * Abstract {@link Executor} that implements most methods as no-ops.
 */
public abstract class AbstractExecutor
        implements Executor {
//
//    public Number executeDelete(StoreQuery q, Object[] params) {
//        return q.getContext().deleteInMemory(q, this, params);
//    }
//
//    public Number executeUpdate(StoreQuery q, Object[] params) {
//        return q.getContext().updateInMemory(q, this, params);
//    }
//
//    public String[] getDataStoreActions(StoreQuery q, Object[] params,
//                                        Range range) {
//        return EMPTY_STRINGS;
//    }
//
//    public void validate(StoreQuery q) {
//    }
//
//
//    public QueryExpressions[] getQueryExpressions() {
//        return null;
//    }
//
//    public ResultShape<?> getResultShape(StoreQuery q) {
//        return null;
//    }
//
//    public void getRange(StoreQuery q, Object[] params, Range range) {
//    }
//
//    public Object getOrderingValue(StoreQuery q, Object[] params,
//                                   Object resultObject, int orderIndex) {
//        return null;
//    }
//
//    public boolean[] getAscending(StoreQuery q) {
//        return EMPTY_BOOLEANS;
//    }
//
//    public boolean isPacking(StoreQuery q) {
//        return false;
//    }
//
//    public String getAlias(StoreQuery q) {
//        return null;
//    }
//
//    public String[] getProjectionAliases(StoreQuery q) {
//        return EMPTY_STRINGS;
//    }
//
//    public Class<?>[] getProjectionTypes(StoreQuery q) {
//        return EMPTY_CLASSES;
//    }
//
//    public ClassMetaData[] getAccessPathMetaDatas(StoreQuery q) {
//        return EMPTY_METAS;
//    }
//
//    public int getOperation(StoreQuery q) {
//        return OP_SELECT;
//    }
//
//    public boolean isAggregate(StoreQuery q) {
//        return false;
//    }
//
//    public boolean isDistinct(StoreQuery q) {
//        return false;
//    }
//
//    public boolean hasGrouping(StoreQuery q) {
//        return false;
//    }
//
//    public OrderedMap<Object,Class<?>> getOrderedParameterTypes(StoreQuery q) {
//        return EMPTY_ORDERED_PARAMS;
//    }
//
//    public LinkedMap getParameterTypes(StoreQuery q) {
//        LinkedMap result = new LinkedMap();
//        result.putAll(getOrderedParameterTypes(q));
//        return result;
//    }
//
//    public Class<?> getResultClass(StoreQuery q) {
//        return null;
//    }
//
//    public Map<FieldMetaData,Value> getUpdates(StoreQuery q) {
//        return null;
//    }
}
