package pt.inevo.encontra;

import java.util.ArrayList;
import java.util.List;
import pt.inevo.encontra.index.Result;
import pt.inevo.encontra.index.ResultSet;
import pt.inevo.encontra.query.QueryCombiner;

/**
 * The simplest Query results combiner. It just takes a group of ResulSet's
 * and combines the results that appear in all of them.
 * @author ricardo
 */
public class SimpleQueryCombiner implements QueryCombiner {

    @Override
    public ResultSet combine(List<ResultSet> results) {

        boolean first = true;
        ResultSet combinedResultSet = null, set1 = null, set2 = null;
        for (int i = 0 ; i < results.size() ; i++){

            if (first){
                if (i + 1 < results.size()){
                    set1 = results.get(i);
                    set2 = results.get(i+1);
                    combinedResultSet = this.combineResultSets(set1, set2);
                    i++;
                } else combinedResultSet = results.get(i);
            } else {
                set1 = combinedResultSet;
                set2 = results.get(i);
                combinedResultSet = this.combineResultSets(set1, set2);
            }
        }

        return combinedResultSet;
    }

    /**
     * Brute force combination of two ResultSet's. Only Result's that appear on
     * both ResultSet's are included in the result ResultSet.
     * @param set1
     * @param set2
     * @return
     */
    private ResultSet combineResultSets(ResultSet set1, ResultSet set2){

        List<Result> combinedResults = new ArrayList<Result>();

        while (set1.hasNext()){

            Result r = set1.getNext();
            if (set2.contains(r)){
                combinedResults.add(r);
            }
        }

        return new ResultSet(combinedResults);
    }

}