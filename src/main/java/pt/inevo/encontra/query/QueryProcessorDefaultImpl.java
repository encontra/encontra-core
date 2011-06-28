package pt.inevo.encontra.query;

import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.common.ResultSetDefaultImpl;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.IndexingException;
import pt.inevo.encontra.index.search.AbstractSearcher;
import pt.inevo.encontra.index.search.Searcher;
import pt.inevo.encontra.query.criteria.CriteriaBuilderImpl;
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.criteria.exps.Constant;
import pt.inevo.encontra.storage.IEntity;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation for the query processor.
 * Cascade Query Processor.
 *
 * @author Ricardo
 * @version 1.0
 */
public class QueryProcessorDefaultImpl<E extends IEntity> extends AbstractQueryProcessor<E> {

    protected ResultSetOperations combiner;

    public QueryProcessorDefaultImpl() {
        this(null);
    }

    public QueryProcessorDefaultImpl(Class clazz) {
        super(clazz);
        combiner = new ResultSetOperations();
        queryParser = new QueryParserDefaultImpl();
        logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void setTopSearcher(AbstractSearcher topSearcher) {
        super.setTopSearcher(topSearcher);
        //the combiner must know the top level object storage
        combiner.setStorage(topSearcher.getObjectStorage());
    }

    /**
     * Processes the AND iteratively.
     *
     * @param node the query parser node to be processed
     * @return a ResultSet with the results from the AND query
     */
    @Override
    protected ResultSet processAND(QueryParserNode node) {
        ResultSet<E> results;

        List resultsParts = new ArrayList<ResultSetDefaultImpl<E>>();
        List<QueryParserNode> nodes = node.childrenNodes;

        for (QueryParserNode n : nodes) {
            ResultSet<E> r = process(n);
            resultsParts.add(r);
        }

        results = combiner.intersect(resultsParts, node.limit, node.criteria);

        return results;
    }

    /**
     * Processes the OR iteratively.
     *
     * @param node the query parser node to be processed
     * @return a ResultSet with the results from the OR query
     */
    @Override
    protected ResultSet processOR(QueryParserNode node) {
        ResultSet<E> results;

        List resultsParts = new ArrayList<ResultSetDefaultImpl<E>>();
        List<QueryParserNode> nodes = node.childrenNodes;
        for (QueryParserNode n : nodes) {
            resultsParts.add(process(n));
        }
        results = combiner.join(resultsParts, node.distinct, node.limit, node.criteria);

        return results;
    }

    @Override
    protected ResultSet processSIMILAR(QueryParserNode node) {
        ResultSet<E> results;

        if (node.field != null) {
            results = processSIMILARSimple(node);
        } else {
            results = processSIMILARCompound(node);
        }

        return results;
    }

    /**
     * The SIMILAR/EQUAL/NOTEQUAL expression is simple (we don't have to break down the object into IndexedObjects).
     *
     * @param node the query parser node to be processed
     * @return a ResultSet with the results from the Query
     */
    protected ResultSet processSIMILARSimple(QueryParserNode node) {
        ResultSet<E> results = null;
        /*
         * must check the path object - see whether it is a field from this
         * class or from other indexed field, in other class
         */
        QueryParserNode pathNode = node.childrenNodes.get(0);

        /**
         * Case: "field" similar 'value'
         */
        if (pathNode.predicate instanceof Constant) {
            results = searcherMap.get(node.field).search(createObjectSubQuery(node));
        } else {
            //must check if the field is from the current class or if it is from a compound model
            Searcher searcher;
            Path queryPath;
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
                searcher = searcherMap.get(parentField);
                Class clazz = parentPath.getJavaType();
                queryPath = new Path(clazz);

                for (Path relPath : relativePaths) {
                    queryPath = queryPath.get(relPath.getAttributeName());
                }

                //in the end we must have the elements we desire
                queryPath = queryPath.get(node.field);

            } else {
                //get the respective searcher
                searcher = searcherMap.get(node.field);
                queryPath = parentPath;
            }

            //perform the sub-query to retrieve the results
            if (node.fieldObject instanceof IndexedObject) {
                results = searcher.search(createExpressionSubQuery(node, queryPath, ((IndexedObject) node.fieldObject).getValue()));
            } else {
                results = searcher.search(createExpressionSubQuery(node, queryPath, node.fieldObject));
            }
        }
        return results;
    }

    /**
     * Must decompose the IEntity into IndexedObjects and the perform the searches.
     *
     * @param node the query parser node to be composed
     * @return a ResultSet with the results from the Query
     */
    protected ResultSet processSIMILARCompound(QueryParserNode node) {
        ResultSet<E> results = new ResultSetDefaultImpl<E>();
        //don't know which searchers to use, so lets dig a bit
        try {
            List<IndexedObject> indexedObjects = indexedObjectFactory.processBean((IEntity) node.fieldObject);
            List resultsParts = new ArrayList<ResultSetDefaultImpl<E>>();
            int initialLimit = node.limit;

            for (IndexedObject obj : indexedObjects) {
                String fieldName = obj.getName();
                Searcher s = searcherMap.get(fieldName);

                CriteriaBuilderImpl cb = new CriteriaBuilderImpl();

                CriteriaQuery query = cb.createQuery(obj.getValue().getClass());
                Path subModelPath;
                Class clazz;
                //detect if the object is a compound one
                if (obj.getValue() instanceof IEntity || obj.getValue() instanceof IndexedObject) {
                    clazz = obj.getValue().getClass();
                    subModelPath = query.from(clazz);
                } else {    //the field is from the current class
                    clazz = node.fieldObject.getClass();
                    subModelPath = query.from(clazz);
                    subModelPath = subModelPath.get(fieldName);
                }

                resultsParts.add(s.search(createExpressionSubQuery(node, subModelPath, obj.getValue())));
            }

            results = combiner.intersect(resultsParts, initialLimit, node.criteria);

        } catch (IndexingException e) {
            String message = "Could not execute the query. Possible reason: " + e.getMessage();
            logger.log(Level.INFO, message);
        }
        return results;
    }

    /**
     * Creates a sub-query for Equal, Similar and NoEqual, given a node
     *
     * @param node the node for which we are creating a sub-query
     * @param path the path to be used in the expression
     * @param obj  the object to be compared with the objects referenced by the path
     * @return
     */
    protected Query createExpressionSubQuery(QueryParserNode node, Path path, Object obj) {
        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        CriteriaQuery q = cb.createQuery(resultClass);
        try {
            Constructor c = node.predicateType.getConstructor(Expression.class, Object.class);
            CriteriaQuery newQuery = q.where((Expression) c.newInstance(path, obj));
            newQuery = newQuery.distinct(node.distinct).limit(node.limit);
            return newQuery;
        } catch (Exception ex) {
            String message = "Could not create the new query! Possible reason: " + ex.getMessage();
            logger.log(Level.SEVERE, message);
            throw new RuntimeException(message); //throw an exception because we can't handle the performed query
        }
    }

    /**
     * Creates a sub-query for Equal, Similar, and NotEqual, given a node.
     * This query is created with the Object constructor (Object, Object).
     * @param node the node to be used in the expression
     * @return the new query to be used
     */
    protected Query createObjectSubQuery(QueryParserNode node) {
        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        try {
            Constructor c = node.predicateType.getConstructor(Object.class, Object.class);
            CriteriaQuery newQuery = cb.createQuery().where((Expression) c.newInstance(node.field, node.fieldObject));
            newQuery = newQuery.distinct(node.distinct).limit(node.limit);
            return newQuery;
        } catch (Exception e) {
            String message = "Could not create the new query! Possible reason: " + e.getMessage();
            logger.log(Level.SEVERE, message);
            throw new RuntimeException(message); //throw an exception because we can't handle the performed query
        }
    }
}
