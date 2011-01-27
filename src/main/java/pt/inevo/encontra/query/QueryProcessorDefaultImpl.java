package pt.inevo.encontra.query;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import pt.inevo.encontra.engine.QueryProcessor;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.IndexingException;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.common.ResultSetDefaultImpl;
import pt.inevo.encontra.index.search.Searcher;
import pt.inevo.encontra.query.criteria.CriteriaBuilderImpl;
import pt.inevo.encontra.query.criteria.CriteriaQueryImpl;
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.criteria.exps.And;
import pt.inevo.encontra.query.criteria.exps.Equal;
import pt.inevo.encontra.query.criteria.exps.NotEqual;
import pt.inevo.encontra.query.criteria.exps.Or;
import pt.inevo.encontra.query.criteria.exps.Similar;
import pt.inevo.encontra.storage.IEntity;

/**
 * Default implementation for the query processor.
 * @author Ricardo
 */
public class QueryProcessorDefaultImpl<E extends IEntity> extends QueryProcessor<E> {

    protected Class resultClass;
    ResultSetOperations combiner;

    public QueryProcessorDefaultImpl() {
        super();
        combiner = new ResultSetOperations();
        queryParser = new QueryParserDefaultImpl();
    }

    @Override
    public ResultSet<E> process(QueryParserNode node) {

        ResultSet<E> results = new ResultSetDefaultImpl<E>();

        if (node.predicateType.equals(And.class)) {
            List resultsParts = new ArrayList<ResultSetDefaultImpl<E>>();
            List<QueryParserNode> nodes = node.childrenNodes;
            for (QueryParserNode n : nodes) {
                resultsParts.add(process(n));
            }
            results = combiner.intersect(resultsParts);
        } else if (node.predicateType.equals(Or.class)) {
            List resultsParts = new ArrayList<ResultSetDefaultImpl<E>>();
            List<QueryParserNode> nodes = node.childrenNodes;
            for (QueryParserNode n : nodes) {
                resultsParts.add(process(n));
            }
            results = combiner.join(resultsParts, node.distinct);
        } else if (node.predicateType.equals(Similar.class)
                || node.predicateType.equals(Equal.class)
                || node.predicateType.equals(NotEqual.class)) {

            if (node.field != null) {
                /*
                 * must check the path object - see wheter it is a field from this
                 * class or from other indexed field, in other class
                 */
                QueryParserNode pathNode = node.childrenNodes.get(0);
                Path p = (Path) pathNode.predicate;

                //track the full path of the desired field
                Queue<Path> relativePaths = new LinkedList<Path>();

                Path parentPath = p.getParentPath();
                if (parentPath.isField()) {     //its not a field of this processor
                    while (parentPath.isField()) {
                        relativePaths.add(parentPath);
                        parentPath = parentPath.getParentPath();
                    }

                    if (relativePaths.size() > 0) {
                        parentPath = relativePaths.remove();
                    } else {
                        parentPath = p;
                    }

                    String parentField = parentPath.getAttributeName();
                    Searcher s = searcherMap.get(parentField);
                    Class clazz = parentPath.getJavaType();
                    Path newQueryPath = new Path(clazz);

                    for (Path relPath : relativePaths) {
                        newQueryPath = newQueryPath.get(relPath.getAttributeName());
                    }

                    //in the end we must have the elements we desire
                    newQueryPath = newQueryPath.get(node.field);
                    return s.search(createSubQuery(node, newQueryPath, node.fieldObject));

                } else {
                    //get the respective searcher
                    Searcher s = searcherMap.get(node.field);
                    results = s.search(createSubQuery(node, parentPath, node.fieldObject));
                }
            } else {
                //dont know which searchers to use, so lets digg a bit
                try {
                    List<IndexedObject> indexedObjects = indexedObjectFactory.processBean((IEntity) node.fieldObject);
                    List resultsParts = new ArrayList<ResultSetDefaultImpl<E>>();
                    for (IndexedObject obj : indexedObjects) {
                        String fieldName = obj.getName();
                        Searcher s = searcherMap.get(fieldName);

                        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();

                        CriteriaQuery query = cb.createQuery(obj.getValue().getClass());
                        Path subModelPath = null;
                        Class clazz = obj.getValue().getClass();
                        //detect if the object is a compound one
                        if (obj.getValue() instanceof IEntity || obj.getValue() instanceof IndexedObject) {
                            clazz = obj.getValue().getClass();
                            subModelPath = query.from(clazz);
                        } else {
                            clazz = node.fieldObject.getClass();
                            subModelPath = query.from(clazz);
                            subModelPath = subModelPath.get(fieldName);
                        }

                        resultsParts.add(s.search(createSubQuery(node, subModelPath, obj.getValue())));
                    }

                    results = combiner.intersect(resultsParts);

                } catch (IndexingException e) {
                    System.out.println("[Error-IndexingException] Possible reason: " + e.getMessage());
                }
            }
        }

        return results;
    }

    //Create a subquery for Equal, Similar and NoEqual, given a node
    private Query createSubQuery(QueryParserNode node, Path path, Object obj) {
        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        CriteriaQuery q = cb.createQuery(resultClass);
        try {
            Constructor c = node.predicateType.getConstructor(Expression.class, Object.class);
            CriteriaQuery newQuery = q.where((Expression) c.newInstance(path, obj));
            newQuery = ((CriteriaQueryImpl) newQuery).distinct(node.distinct);
            return newQuery;
        } catch (Exception ex) {
            System.out.println("[Error]: Could not execute the query! Possible reason: " + ex.getMessage());
        }
        return q;
    }
}

/**
 * Implementation of Boolean Operations with ResultSets.
 * @author Ricardo
 * @param <E>
 */
class ResultSetOperations<E extends IEntity> {

    /**
     * Applies boolean operation AND to the list of ResultSets.
     * @param results the list where to apply the AND operation
     * @return
     */
    public ResultSet<E> intersect(List<ResultSet<E>> results) {

        //final results
        ResultSet combinedResultSet = new ResultSetDefaultImpl();

        //invert and normalize all the results
        for (ResultSet set: results){
            set.invertScores();
            set.normalizeScores();
        }

        for (ResultSet set: results) {
            Iterator<Result> it = set.iterator();
            while (it.hasNext()){
                Result r = it.next();

                if (!combinedResultSet.containsResultObject(r.getResultObject())) {
                    boolean contains = true;
                    double score = r.getScore();
                    for (ResultSet s: results) {
                        if (!s.containsResultObject(r.getResultObject())) {
                            contains = false;
                            break;
                        } else {
                            score = s.getScore(r.getResultObject());
                        }
                    }

                    if (contains) {
                        Result newResult = new Result(r.getResultObject());
                        newResult.setScore(score/results.size());
                        combinedResultSet.add(newResult);
                    }
                }
            }
        }

        return combinedResultSet;
    }

    /**
     * Applies boolean operation OR to the list of ResultSets.
     * @param results the list where to apply the OR operation
     * @return
     */
    public ResultSet<E> join(List<ResultSetDefaultImpl<E>> results, boolean distinct) {

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

        return combinedResultSet;
    }
}
