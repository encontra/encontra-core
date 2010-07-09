package pt.inevo.encontra.index.search;

import pt.inevo.encontra.engine.Engine;
import pt.inevo.encontra.index.Index;
import pt.inevo.encontra.index.IndexEntry;
import pt.inevo.encontra.index.ResultSet;
import pt.inevo.encontra.query.Query;
import pt.inevo.encontra.query.QueryCombiner;
import pt.inevo.encontra.query.SimpleQueryCombiner;
import pt.inevo.encontra.storage.SimpleObjectStorage;
import pt.inevo.encontra.storage.StorableObject;

import java.util.ArrayList;
import java.util.List;


public class SimpleCombinedSearcher<E extends IndexEntry> implements Searcher<E> {
    List<Searcher> searchers;

    /**
     * The current Query Combiner - helps combining the results from the
     * queries realized.
     */
    protected QueryCombiner combiner;

    public SimpleCombinedSearcher(){

    }

    public void setQueryCombiner(QueryCombiner combiner){
        this.combiner = combiner;
    }

    public QueryCombiner getQueryCombiner() {
        return combiner;
    }

    public void setSearchers(List<Searcher> searchers){
        this.searchers=searchers;
    }

    @Override
    public ResultSet<E> search(Query query) {

        List<ResultSet> results = new ArrayList<ResultSet>();
        //sends the query to all the indexes that support that query type
        for (Searcher s : searchers) {
                results.add(s.search(query)); //getObjectResults(idx,idx.search()));
        }

        return combiner.combine(results);
    }

    @Override
    public ResultSet<E> search(Query[] queries) {

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