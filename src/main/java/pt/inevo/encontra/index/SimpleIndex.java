package pt.inevo.encontra.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pt.inevo.encontra.query.Query;
import pt.inevo.encontra.query.Query.QueryType;
import pt.inevo.encontra.query.RangeQuery;

/**
 * A linear implementation of an Index.
 * @author ricardo
 */
public class SimpleIndex extends MemoryIndex {

    protected ArrayList<AbstractObject> idx;
    protected static QueryType [] supportedTypes  =
            new QueryType[]{QueryType.RANDOM, QueryType.RANGE,
                                QueryType.TEXT, QueryType.KNN,
                                QueryType.BOOLEAN};

    public SimpleIndex() {
        idx = new ArrayList<AbstractObject>();
    }

    @Override
    public boolean insertObject(AbstractObject obj) {
        idx.add(obj);
        return true;
    }

    @Override
    public boolean removeObject(AbstractObject obj) {
        idx.remove(obj);
        return true;
    }

    @Override
    public boolean contains(AbstractObject object){
        if (idx.contains(object)){
            return true;
        }
        return false;
    }

    @Override
    public List<AbstractObject> getAllObjects() {
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
        for (int i = 0; i < 7; i++) {
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

        for (AbstractObject o: idx){
            //TO DO - must check if the object is in range.
//            if (obj.inRange(o, range)){
//                res.add(new Result(o));
//            }
        }
        results.setResults(res);

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
