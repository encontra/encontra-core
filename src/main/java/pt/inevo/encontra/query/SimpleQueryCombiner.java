package pt.inevo.encontra.query;

import java.util.ArrayList;
import java.util.List;
import pt.inevo.encontra.index.Result;
import pt.inevo.encontra.index.ResultSet;

/**
 * The simplest Query results combiner. It just takes a group of ResulSet's
 * and combines the results that appear in all of them.
 * @author ricardo
 */
public class SimpleQueryCombiner implements QueryCombiner {

    @Override
    public ResultSet combine(List<ResultSet> results) {

        boolean first = true;
        ResultSet combinedResultSet = new ResultSet(), set1 = null, set2 = null;
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
    @SuppressWarnings({"unchecked"})
    private ResultSet combineResultSets(ResultSet<?> set1, ResultSet set2){

        List<Result> combinedResults = new ArrayList<Result>();

        for(Result r1: set1){
            if (set2.contains(r1)){
                Result r2= set2.get(set2.indexOf(r1));
                Result n = new Result(r1.getResult());
                n.setSimilarity(r1.getSimilarity()*r2.getSimilarity());
                combinedResults.add(n);
            }
        }

        return new ResultSet(combinedResults);
    }

}