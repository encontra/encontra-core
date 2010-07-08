package pt.inevo.encontra.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pt.inevo.encontra.query.Query;
import pt.inevo.encontra.query.Query.QueryType;

/**
 * A linear implementation of an Index. For now only accepts Random queries.
 * @author ricardo
 */
public class LinearIndex<O extends AbstractObject & IndexEntry> extends ArrayList<O> implements MemoryIndex<O> {


    public LinearIndex() {
        super();
    }

    @Override
    public boolean insert(O obj) {
        super.add(obj);
        return true;
    }

    @Override
    public boolean remove(O obj) {
        super.remove(obj);
        return true;
    }

    @Override
    public List<O> getAll() {
        return this;
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
                int position = r.nextInt(size());
                res.add(new Result<O>(get(position)));
            }
            return new ResultSet(res);
        } else {
            return null;
        }
    }
}
