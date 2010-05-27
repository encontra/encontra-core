/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.inevo.encontra;

import java.util.ArrayList;
import java.util.List;
import pt.inevo.encontra.index.Index;
import pt.inevo.encontra.index.ResultSet;
import pt.inevo.encontra.query.Query;

/**
 * A generic and simple engine that uses a SimpleQueryCombiner to combine
 * the results of the queries realized to it.
 * @author ricardo
 */
class SimpleEngine extends Engine {

    SimpleEngine() {
        combiner = new SimpleQueryCombiner();
    }

    @Override
    public ResultSet search(Query query) {

        List<ResultSet> results = new ArrayList<ResultSet>();

        for (Index idx : indexes) {
            if (idx.supportsQueryType(query.getType())) { //support type then make the query
                results.add(idx.search(query));
            }
        }

        System.out.println(results.toString());

        return combiner.combine(results);
    }

    @Override
    public ResultSet search(Query[] queries) {

        List<ResultSet> results = new ArrayList<ResultSet>();

        for (int i = 0; i < queries.length; i++) {
            results.add(search(queries[i]));
        }

        return combiner.combine(results);
    }
}