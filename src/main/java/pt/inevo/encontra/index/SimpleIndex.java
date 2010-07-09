package pt.inevo.encontra.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pt.inevo.encontra.descriptors.Key;
import pt.inevo.encontra.query.Query;
import pt.inevo.encontra.query.Query.QueryType;
import pt.inevo.encontra.query.RangeQuery;

/**
 * A linear implementation of an Index.
 * @author ricardo
 */
public class SimpleIndex<E extends IndexEntry> implements MemoryIndex<E> {

    protected ArrayList<E> idx;
    protected static QueryType [] supportedTypes  =
            new QueryType[]{QueryType.RANDOM, QueryType.RANGE,
                                QueryType.TEXT, QueryType.KNN,
                                QueryType.BOOLEAN};

    class SimpleIndexComparator implements IndexComparator<Key<Double>, AbstractObject>{

        @Override
        public int indexCompare(Key<Double> k, AbstractObject o) {
            Key<Double> key = extractKey(o);
            return compare(k, key);
        }

        @Override
        public Key<Double> extractKey(AbstractObject object) {
            return new Key(Double.valueOf(object.getId().toString()));
        }

        @Override
        public int compare(Key<Double> t, Key<Double> t1) {
            return Double.compare(t.getKeyValue(), t1.getKeyValue());
        }

    }

    public SimpleIndex() {
        idx = new ArrayList<E>();
    }


    @Override
    public boolean insert(E entry) {
        return idx.add(entry);
    }

    @Override
    public boolean remove(E entry) {
        return idx.remove(entry);
    }

    @Override
    public int size() {
        return idx.size();
    }

    @Override
    public E get(int i) {
        return idx.get(i);
    }

    @Override
    public boolean contains(E object){
        if (idx.contains(object)){
            return true;
        }
        return false;
    }

    @Override
    public List<E> getAll() {
        return idx;
    }

    @Override
    public QueryType[] getSupportedQueryTypes() {
        return supportedTypes;
    }

    @Override
    public boolean supportsQueryType(QueryType type) {
        for (QueryType t: supportedTypes){
            if (t.equals(type)){
                return true;
            }
        }
        return false;
    }

    @Override
    public ResultSet search(Query query) {
        if (query.getType().equals(Query.QueryType.RANDOM)) {
            return performRandomQuery();
        } else if (query.getType().equals(Query.QueryType.RANGE)) {
            return performRangeQuery(query);
        } else if (query.getType().equals(Query.QueryType.KNN)){
            return performKnnQuery(query);
        } else if (query.getType().equals(Query.QueryType.TEXT)){
            return performTextquery(query);
        } else if (query.getType().equals(Query.QueryType.BOOLEAN)) {
            return performBooleanQuery(query);
        }
        else {
            return new ResultSet();
        }
    }

    private ResultSet performRandomQuery() {
        ArrayList<Result> res = new ArrayList<Result>();
        Random r = new Random();
        for (int i = 0; i < r.nextInt(idx.size()); i++) {
            int position = r.nextInt(idx.size());
            res.add(new Result(idx.get(position)));
        }
        return new ResultSet(res);
    }

    private ResultSet performRangeQuery(Query query) {
        ArrayList<Result> res = new ArrayList<Result>();
        ResultSet results = new ResultSet();

        RangeQuery q = (RangeQuery)query;
        double range = q.getRange();
        AbstractObject obj = q.getQueryObject();

        for (E o: idx){
            //TO DO - must check if the object is in range.
//            if (obj.inRange(o, range)){
//                res.add(new Result(o));
//            }
        }
        results.addAll(res);

        return results;
    }

    private ResultSet performKnnQuery(Query query){
        //TO DO
        return new ResultSet();
    }

    private ResultSet performTextquery(Query query){
        //TO DO
        return new ResultSet();
    }

    private ResultSet performBooleanQuery(Query query){
        //TO DO
        return new ResultSet();
    }
}
