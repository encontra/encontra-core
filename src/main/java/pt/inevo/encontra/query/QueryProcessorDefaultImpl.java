package pt.inevo.encontra.query;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import pt.inevo.encontra.engine.QueryProcessor;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.IndexingException;
import pt.inevo.encontra.index.ResultSet;
import pt.inevo.encontra.index.search.Searcher;
import pt.inevo.encontra.query.criteria.CriteriaBuilderImpl;
import pt.inevo.encontra.query.criteria.CriteriaQueryImpl;
import pt.inevo.encontra.query.criteria.exps.And;
import pt.inevo.encontra.query.criteria.exps.Similar;
import pt.inevo.encontra.storage.IEntity;

/**
 * Default implementation for the query processor.
 * @author Ricardo
 */
public class QueryProcessorDefaultImpl<E extends IndexedObject> extends QueryProcessor<E> {

    protected Class resultClass;
    protected QueryCombiner<E> combiner;

    public QueryProcessorDefaultImpl() {
        super();
        combiner = new SimpleQueryCombiner();
        queryParser = new QueryParserDefaultImpl();
    }

    @Override
    public ResultSet process(QueryParserNode node) {
        List<ResultSet<E>> results = new ArrayList<ResultSet<E>>();

        if (node.predicateType.equals(And.class)) {

            List<QueryParserNode> nodes = node.childrenNodes;
            for (QueryParserNode n : nodes) {
                results.add(process(n));
            }
        } else if (node.predicateType.equals(Similar.class)) {

            //its not a simple field in the indexed model
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

                    if (relativePaths.size() > 0)
                        parentPath = relativePaths.remove();
                    else parentPath = p;

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

                    CriteriaQuery newQuery = criteriaImpl.where(new Similar(newQueryPath, node.fieldObject));
                    return s.search(newQuery);


                } else {
                    //get the respective searcher
                    Searcher s = searcherMap.get(node.field);
                    //creating a simpler CriteriaQuery only with Similar desired
                    CriteriaQueryImpl criteriaImpl = new CriteriaQueryImpl(resultClass);
                    CriteriaQuery newQuery = criteriaImpl.where(node.predicate);
                    return s.search(newQuery);
                }
            } else {
                //dont know which searchers to use, so lets digg a bit
                try {
                    List<IndexedObject> indexedObjects = indexedObjectFactory.processBean((IEntity) node.fieldObject);
                    for (IndexedObject obj : indexedObjects) {
                        String fieldName = obj.getName();
                        Searcher s = searcherMap.get(fieldName);

                        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();

                        CriteriaQuery query = cb.createQuery(obj.getValue().getClass());
                        Path subModelPath = query.from(obj.getValue().getClass());
                        query = query.where(new Similar(subModelPath, obj.getValue()));

                        results.add(s.search(query));
                    }

                } catch (IndexingException e) {
                    System.out.println("Exception: " + e.getMessage());
                }
            }
        }

        // TODO remove this from here, because this is always making an AND operation
        // TODO this should be accomplished using the highlevel objects?
        return combiner.combine(results);
    }
}
