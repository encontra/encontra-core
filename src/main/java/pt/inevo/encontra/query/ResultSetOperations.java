package pt.inevo.encontra.query;

import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.common.ResultSetDefaultImpl;
import pt.inevo.encontra.query.criteria.StorageCriteria;
import pt.inevo.encontra.storage.EntityStorage;
import pt.inevo.encontra.storage.IEntity;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Implementation of Boolean Operations with ResultSets.
 * @author Ricardo
 * @param <E>
 */
public class ResultSetOperations<E extends IEntity> {

    private EntityStorage storage;

    public ResultSetOperations(){}

    public ResultSetOperations(EntityStorage storage){
        this.storage = storage;
    }

    public void setStorage(EntityStorage storage){
        this.storage = storage;
    }

    /**
     * Applies boolean operation AND to the list of ResultSets.
     * @return results the list where to apply the AND operation
     */
    public ResultSet<E> intersect(List<ResultSet<E>> results, int limit, StorageCriteria criteria) {

        //final results
        ResultSet<E> combinedResultSet = new ResultSetDefaultImpl<E>();
        ResultSet<E> notContained = new ResultSetDefaultImpl<E>();

        List<Serializable> validIds = null;
        if (criteria != null){
            validIds = storage.getValidIds(criteria);
        }

        //invert and normalize all the results
        for (ResultSet set : results) {
            set.invertScores();
            set.normalizeScores();
        }

        for (ResultSet set : results) {

            Iterator<Result> it = set.iterator();
            while (it.hasNext()) {
                Result r = it.next();
                Object resultObject = r.getResultObject();

                if (criteria != null && !validIds.contains(((IEntity)resultObject).getId())){
                    notContained.add(r);
                    continue;
                }

                if (!notContained.containsResultObject(resultObject) &&
                        !combinedResultSet.containsResultObject(resultObject)) {
                    boolean contains = true;
                    double score = r.getScore();
                    for (ResultSet s : results) {
                        if (!s.containsResultObject(resultObject)) {
                            contains = false;
                            notContained.add(r);
                            break;
                        } else {
                            score = s.getScore(resultObject);
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
    public ResultSet<E> join(List<ResultSetDefaultImpl<E>> results, boolean distinct, int limit, StorageCriteria criteria) {

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
                        if (criteria != null){
                            if (storage.validate(((IEntity)(r.getResultObject())).getId(), criteria)){
                                combinedResultSet.add(r);
                            }
                        } else {
                            combinedResultSet.add(r);
                        }
                    }
                } else {
                    if (criteria != null){
                        if (storage.validate(((IEntity)(r.getResultObject())).getId(), criteria)){
                            combinedResultSet.add(r);
                        }
                    } else {
                        combinedResultSet.add(r);
                    }
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
