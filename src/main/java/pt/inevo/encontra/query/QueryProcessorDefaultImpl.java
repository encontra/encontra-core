package pt.inevo.encontra.query;

import java.util.ArrayList;
import java.util.List;
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
        return performQuery(node);
    }

    //should have different 'strategies' to perform the combination
    private ResultSet performQuery(QueryParserNode node) {

        List<ResultSet<E>> results = new ArrayList<ResultSet<E>>();

        if (node.predicateType.equals(And.class)) {

            List<QueryParserNode> nodes = node.childrenNodes;
            for (QueryParserNode n : nodes) {
                results.add(performQuery(n));
            }
        } else if (node.predicateType.equals(Similar.class)) {

            if (node.field != null) {
                //get the respective searcher
                Searcher s = searcherMap.get(node.field);
                //creating a simpler CriteriaQuery only with Similar desired
                CriteriaQueryImpl criteriaImpl = new CriteriaQueryImpl(resultClass);
                CriteriaQuery newQuery = criteriaImpl.where(node.predicate);
                return s.search(newQuery);
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
