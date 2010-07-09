package pt.inevo.encontra.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pt.inevo.encontra.index.IndexEntry;
import pt.inevo.encontra.index.Result;
import pt.inevo.encontra.query.QueryCombiner;
import pt.inevo.encontra.query.SimpleQueryCombiner;
import pt.inevo.encontra.index.Index;
import pt.inevo.encontra.index.ResultSet;
import pt.inevo.encontra.query.Query;
import pt.inevo.encontra.storage.IEntry;
import pt.inevo.encontra.storage.ObjectStorage;
import pt.inevo.encontra.storage.SimpleObjectStorage;
import pt.inevo.encontra.storage.StorableObject;

/**
 * A generic and simple engine that uses a SimpleQueryCombiner to combine
 * the results of the queries realized to it.
 * @author ricardo
 */
public class SimpleEngine<O extends StorableObject> extends Engine<O> {

    public SimpleEngine() {
        super();
    }

    @Override
    public ResultSet<O> search(Query query) {

        List<ResultSet> results = new ArrayList<ResultSet>();
        //sends the query to all the indexes that support that query type
        for (Index idx : indexes) {
            if (idx.supportsQueryType(query.getType())) { //if supports type then make the query
                results.add(getObjectResults(idx,idx.search(query)));
            }
        }

        return combiner.combine(results);
    }

    @Override
    public ResultSet<O> search(Query[] queries) {

        List<ResultSet> results = new ArrayList<ResultSet>();

        for (int i = 0; i < queries.length; i++) {
            ResultSet set = search(queries[i]);
            if (set.size() != 0){    //if doesn't return results than skip it
                results.add(set);
            }

        }

        return combiner.combine(results);
    }


}