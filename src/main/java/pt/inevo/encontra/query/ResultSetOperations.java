package pt.inevo.encontra.query;

import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.common.ResultSetDefaultImpl;
import pt.inevo.encontra.storage.IEntity;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of Boolean Operations with ResultSets.
 * @author Ricardo
 * @param <E>
 */
public class ResultSetOperations<E extends IEntity> {

    /**
     * Applies boolean operation AND to the list of ResultSets.
     * @return results the list where to apply the AND operation
     */
    public ResultSet<E> intersect(List<ResultSet<E>> results, int limit) {

        //final results
        ResultSet combinedResultSet = new ResultSetDefaultImpl();

        //invert and normalize all the results
        for (ResultSet set : results) {
            set.invertScores();
            set.normalizeScores();
        }

        for (ResultSet set : results) {
            Iterator<Result> it = set.iterator();
            while (it.hasNext()) {
                Result r = it.next();

                if (!combinedResultSet.containsResultObject(r.getResultObject())) {
                    boolean contains = true;
                    double score = r.getScore();
                    for (ResultSet s : results) {
                        if (!s.containsResultObject(r.getResultObject())) {
                            contains = false;
                            break;
                        } else {
                            score = s.getScore(r.getResultObject());
                        }
                    }

                    if (contains) {
                        Result newResult = new Result(r.getResultObject());
                        newResult.setScore(score / results.size());
                        combinedResultSet.add(newResult);
                    }
                }
            }
        }

        return combinedResultSet.getFirstResults(limit);
    }

    /**
     * Applies boolean operation OR to the list of ResultSets.
     * @param results the list where to apply the OR operation
     * @return
     */
    public ResultSet<E> join(List<ResultSetDefaultImpl<E>> results, boolean distinct, int limit) {

        //final resultset
        ResultSet combinedResultSet = new ResultSetDefaultImpl();

        //let's get the results
        for (ResultSet set : results) {
            set.invertScores();
            set.normalizeScores();
            Iterator<Result> it = set.iterator();
            while (it.hasNext()) {
                Result r = it.next();
                if (distinct) {
                    if (!combinedResultSet.containsResultObject(r.getResultObject())) {
                        combinedResultSet.add(r);
                    }
                } else {
                    combinedResultSet.add(r);
                }
            }
        }

        //now let's set the scores correctly
        Iterator<Result> it = combinedResultSet.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            double resultScore = r.getScore();
            int found = 1;
            for (ResultSet set : results) {
                if (set.containsResultObject(r.getResultObject())) {
                    resultScore += set.getScore(r.getResultObject());
                    found++;
                }
            }
            resultScore /= found;
            r.setScore(resultScore);
        }

        return combinedResultSet.getFirstResults(limit);
    }
}
