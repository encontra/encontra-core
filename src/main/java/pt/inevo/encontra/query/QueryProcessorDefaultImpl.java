package pt.inevo.encontra.query;

import java.lang.reflect.Constructor;
import java.util.*;

import pt.inevo.encontra.engine.QueryProcessor;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.IndexingException;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.common.ResultSetDefaultImpl;
import pt.inevo.encontra.index.search.AbstractSearcher;
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
    protected ResultSetOperations combiner;

    public QueryProcessorDefaultImpl() {
        super();
        combiner = new ResultSetOperations();
        queryParser = new QueryParserDefaultImpl();
    }

    @Override
    public void setTopSearcher(AbstractSearcher topSearcher) {
        super.setTopSearcher(topSearcher);
        combiner.setStorage(topSearcher.getObjectStorage());
    }

    @Override
    public ResultSet<E> process(QueryParserNode node) {

        ResultSet<E> results = new ResultSetDefaultImpl<E>();

        if (node.predicateType.equals(And.class)) {
            List resultsParts = new ArrayList<ResultSetDefaultImpl<E>>();
            List<QueryParserNode> nodes = node.childrenNodes;

            for (QueryParserNode n : nodes) {
                ResultSet<E> r = process(n);
                resultsParts.add(r);
            }

            results = combiner.intersect(resultsParts, node.limit, node.criteria);

            //this should be the real code, because can handle the searchers that don't retrieve enough results
//            int previousResultSize = -1, partsSize = 0, previousPartsSize = -1;
//
//            for (int newLimit = node.limit*2; results.getSize() < node.limit && previousResultSize < results.getSize()
//                    && previousPartsSize < partsSize ; newLimit *= 2, resultsParts.clear()) {
//                previousResultSize = (results.getSize() == 0 ? -1 : results.getSize());
//                previousPartsSize = partsSize;
//                partsSize = 0;
//                for (QueryParserNode n : nodes) {
//                    n.limit = newLimit;   //lets increase the limit to speed up the combination
//                    ResultSet<E> r = process(n);
//                    partsSize += r.getSize();
//                    resultsParts.add(r);
//                }
//                results = combiner.intersect(resultsParts, node.limit, node.criteria);
//            }
        } else if (node.predicateType.equals(Or.class)) {
            List resultsParts = new ArrayList<ResultSetDefaultImpl<E>>();
            List<QueryParserNode> nodes = node.childrenNodes;
            for (QueryParserNode n : nodes) {
                resultsParts.add(process(n));
            }
            results = combiner.join(resultsParts, node.distinct, node.limit, node.criteria);
        } else if (node.predicateType.equals(Similar.class)
                || node.predicateType.equals(Equal.class)
                || node.predicateType.equals(NotEqual.class)) {

            if (node.field != null) {
                /*
                 * must check the path object - see whether it is a field from this
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
                    results = s.search(createSubQuery(node, newQueryPath, node.fieldObject));

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
                    int previousResultSize = -1;
                    int initialLimit = node.limit;

                    for ( ; results.getSize() < node.limit && previousResultSize < results.getSize();
                          previousResultSize = results.getSize(), node.limit *= 2) {

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

                        results = combiner.intersect(resultsParts, initialLimit, node.criteria);
                    }

                } catch (IndexingException e) {
                    System.out.println("[Error-IndexingException] Possible reason: " + e.getMessage());
                }
            }
        }

        return results.getFirstResults(node.limit);
    }

    //Creates a sub-query for Equal, Similar and NoEqual, given a node
    private Query createSubQuery(QueryParserNode node, Path path, Object obj) {
        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        CriteriaQuery q = cb.createQuery(resultClass);
        try {
            Constructor c = node.predicateType.getConstructor(Expression.class, Object.class);
            CriteriaQuery newQuery = q.where((Expression) c.newInstance(path, obj));
            newQuery = ((CriteriaQueryImpl) newQuery).distinct(node.distinct).limit(node.limit);
            return newQuery;
        } catch (Exception ex) {
            System.out.println("[Error]: Could not execute the query! Possible reason: " + ex.getMessage());
        }
        return q;
    }
}
