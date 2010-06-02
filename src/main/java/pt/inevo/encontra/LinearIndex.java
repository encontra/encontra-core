package pt.inevo.encontra;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pt.inevo.encontra.index.AbstractObject;
import pt.inevo.encontra.index.MemoryIndex;
import pt.inevo.encontra.index.Result;
import pt.inevo.encontra.index.ResultSet;
import pt.inevo.encontra.query.Query;
import pt.inevo.encontra.query.Query.QueryType;

/**
 * A linear implementation of an Index. For now only accepts Random queries.
 * @author ricardo
 */
public class LinearIndex extends MemoryIndex {

    protected ArrayList<AbstractObject> idx;

    public LinearIndex() {
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
    public List<AbstractObject> getAllObjects() {
        return idx;
    }

    @Override
    public QueryType[] getSupportedQueryTypes() {
        return new QueryType[]{Query.QueryType.RANDOM};
    }

    @Override
    public boolean supportsQueryType(QueryType type) {
        return type.equals(QueryType.RANDOM);
    }

    @Override
    public ResultSet search(Query query) {
        if (query.getType().equals(Query.QueryType.RANDOM)) {

            ArrayList<Result> res = new ArrayList<Result>();
            Random r = new Random();
            for (int i = 0; i < 7; i++) {
                int position = r.nextInt(idx.size());
                res.add(new Result(idx.get(position)));
            }
            return new ResultSet(res);
        } else {
            return null;
        }
    }
}
