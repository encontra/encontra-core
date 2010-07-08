package pt.inevo.encontra.query;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Simple Query Sorter. Uses the natural comparator between Query Orders.
 * Queries with lower order will be executed first, and queries with higher order
 * will be executed later.
 * @author ricardo
 */
public class SimpleQuerySorter implements QuerySorter {

    class SimpleQueryComparator implements Comparator<Query> {

        @Override
        public int compare(Query t, Query t1) {
            int query1OrderValue = t.getOrder().getValue();
            int query2OrderValue = t.getOrder().getValue();
            
            if (query1OrderValue > query2OrderValue){
                return -1;
            } else if (query1OrderValue < query2OrderValue){
                return 1;
            }
            return 0;
        }
    }

    @Override
    public List<Query> sort(List<Query> queries) {
        Collections.sort(queries, new SimpleQueryComparator());
        return queries;
    }
}