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
import pt.inevo.encontra.index.Result;
import pt.inevo.encontra.index.ResultSet;
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
    public ResultSet process(QueryParserNode node) {

        ResultSet<E> results = new ResultSet<E>();

        if (node.predicateType.equals(And.class)) {
            List<ResultSet<E>> resultsParts = new ArrayList<ResultSet<E>>();
            List<QueryParserNode> nodes = node.childrenNodes;
            for (QueryParserNode n : nodes) {
                resultsParts.add(process(n));
            }
            results = combiner.intersect(resultsParts);
        } else if (node.predicateType.equals(Or.class)) {
            List<ResultSet<E>> resultsParts = new ArrayList<ResultSet<E>>();
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
                if (parentPath.isField()) {
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
                    CriteriaQueryImpl criteriaImpl = new CriteriaQueryImpl(resultClass);
                    Class clazz = parentPath.getJavaType();
                    Path newQueryPath = new Path(clazz);

                    for (Path relPath : relativePaths) {
                        newQueryPath = newQueryPath.get(relPath.getAttributeName());
                    }

                    //in the end we must have the elements we desire
                    newQueryPath = newQueryPath.get(node.field);
                    try {
                        Constructor c = node.predicateType.getConstructor(Expression.class, Object.class);
                        CriteriaQuery newQuery = criteriaImpl.where((Expression) c.newInstance(newQueryPath, node.fieldObject));
                        newQuery = ((CriteriaQueryImpl) newQuery).distinct(node.distinct);
                        return s.search(newQuery);
                    } catch (Exception ex) {
                        System.out.println("[Error]: Could not execute the query! Possible reason: " + ex.getMessage());
                    }
                } else {
                    //get the respective searcher
                    Searcher s = searcherMap.get(node.field);
                    //creating a simpler CriteriaQuery only with Similar desired
                    CriteriaQueryImpl criteriaImpl = new CriteriaQueryImpl(resultClass);

                    try {
                        Constructor c = node.predicateType.getConstructor(Expression.class, Object.class);
                        CriteriaQuery newQuery = criteriaImpl.where((Expression) c.newInstance(parentPath, node.fieldObject));
                        newQuery = ((CriteriaQueryImpl) newQuery).distinct(node.distinct);
                        results = s.search(newQuery);
                    } catch (Exception ex) {
                        System.out.println("[Error]: Could not execute the query! Possible reason: " + ex.getMessage());
                    }
                }
            } else {
                //dont know which searchers to use, so lets digg a bit
                try {
                    List<IndexedObject> indexedObjects = indexedObjectFactory.processBean((IEntity) node.fieldObject);
                    List<ResultSet<E>> resultsParts = new ArrayList<ResultSet<E>>();
                    for (IndexedObject obj : indexedObjects) {
                        String fieldName = obj.getName();
                        Searcher s = searcherMap.get(fieldName);

                        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();

                        CriteriaQuery query = cb.createQuery(obj.getValue().getClass());
                        Path subModelPath = null;
                        Class clazz = obj.getValue().getClass();
                        if (!clazz.isPrimitive() && !clazz.getName().contains("String")) {
                            clazz = obj.getValue().getClass();
                            subModelPath = query.from(clazz);
                        } else {
                            clazz = node.fieldObject.getClass();
                            subModelPath = query.from(clazz);
                            subModelPath = subModelPath.get(fieldName);
                        }

                        try {
                            Constructor c = node.predicateType.getConstructor(Expression.class, Object.class);
                            query = query.where((Expression) c.newInstance(subModelPath, obj.getValue()));
                            query = ((CriteriaQueryImpl) query).distinct(node.distinct);
                            resultsParts.add(s.search(query));

                        } catch (Exception ex) {
                            System.out.println("[Error]: Could not execute the query! Possible reason: " + ex.getMessage());
                        }
                    }

                    results = combiner.intersect(resultsParts);

                } catch (IndexingException e) {
                    System.out.println("[Error-IndexingException] Possible reason: " + e.getMessage());
                }
            }
        }
        
        results.sort();
        return results;
    }
}

/**
 * Implementation of Boolean Operations with ResultSets.
 * @author Ricardo
 * @param <E>
 */
class ResultSetOperations<E> {

    /**
     * Applies boolean operation AND to the list of ResultSets.
     * @param results the list where to apply the AND operation
     * @return
     */
    public ResultSet<E> intersect(List<ResultSet<E>> results) {

        boolean first = true;
        ResultSet combinedResultSet = new ResultSet(), set1 = null, set2 = null;
        for (int i = 0; i < results.size(); i++) {

            if (first) {
                if (i + 1 < results.size()) {
                    set1 = results.get(i);
                    set2 = results.get(i + 1);
                    combinedResultSet = this.intersect(set1, set2);
                    i++;
                } else {
                    combinedResultSet = results.get(i);
                }
            } else {
                set1 = combinedResultSet;
                set2 = results.get(i);
                combinedResultSet = this.intersect(set1, set2);
            }
        }

        return combinedResultSet;
    }

    /**
     * Applies boolean operation OR to the list of ResultSets.
     * @param results the list where to apply the OR operation
     * @return
     */
    public ResultSet<E> join(List<ResultSet<E>> results, boolean distinct) {

        ResultSet combinedResultSet = new ResultSet();
        for (ResultSet set : results) {
            Iterator<Result> it = set.iterator();
            while (it.hasNext()) {
                Result r = it.next();
                if (distinct) {
                    if (!combinedResultSet.contains(r)) {
                        combinedResultSet.add(r);
                    }
                } else {
                    combinedResultSet.add(r);
                }
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
    public ResultSet intersect(ResultSet<?> set1, ResultSet set2) {

        List<Result> combinedResults = new ArrayList<Result>();

        for (Result r1 : set1) {
            if (set2.contains(r1)) {
                Result r2 = set2.get(set2.indexOf(r1));
                Result n = new Result(r1.getResult());
                n.setSimilarity(r1.getSimilarity() * r2.getSimilarity());
                combinedResults.add(n);
            }
        }

        return new ResultSet(combinedResults);
    }
}
