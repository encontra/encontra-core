package pt.inevo.encontra.engine;

import java.util.ArrayList;
import java.util.List;
import pt.inevo.encontra.query.SimpleQueryCombiner;
import pt.inevo.encontra.index.Index;
import pt.inevo.encontra.index.ResultSet;
import pt.inevo.encontra.query.Query;

/**
 * A generic and simple engine that uses a SimpleQueryCombiner to combine
 * the results of the queries realized to it.
 * @author ricardo
 */
public class SimpleEngine extends Engine {

    public SimpleEngine() {
        combiner = new SimpleQueryCombiner();
    }

    @Override
    public ResultSet search(Query query) {

        List<ResultSet> results = new ArrayList<ResultSet>();
        //sends the query to all the indexes that support that query type
        for (Index idx : indexes) {
            if (idx.supportsQueryType(query.getType())) { //if supports type then make the query
                results.add(idx.search(query));
            }
        }

        return combiner.combine(results);
    }

    @Override
    public ResultSet search(Query[] queries) {

        List<ResultSet> results = new ArrayList<ResultSet>();

        for (int i = 0; i < queries.length; i++) {
            ResultSet set = search(queries[i]);
            if (set.getSize() != 0){    //if doesn't return results than skip it
                results.add(set);
            }

        }

        return combiner.combine(results);
    }
}