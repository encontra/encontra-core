package pt.inevo.encontra.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import pt.inevo.encontra.engine.QueryProcessor;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.ResultSet;
import pt.inevo.encontra.index.search.Searcher;
import pt.inevo.encontra.query.criteria.CriteriaQueryImpl;
import pt.inevo.encontra.query.criteria.exps.And;
import pt.inevo.encontra.query.criteria.exps.Similar;

/**
 * Default implementation for the query processor.
 * @author Ricardo
 */
public class QueryProcessorDefaultImpl<E extends IndexedObject> extends QueryProcessor<E> {

    protected Class resultClass;
    protected Iterator<QueryParserNode> it;

    public QueryProcessorDefaultImpl() {
        super();
        combiner = new SimpleQueryCombiner();
        queryParser = new QueryParserDefaultImpl();
    }

    @Override
    public ResultSet process(Stack<QueryParserNode> node) {
        it = node.iterator();
        return performQuery(it.next());
    }

    //should have different 'strategies' to perform the combination
    private ResultSet performQuery(QueryParserNode node){
        
        List<ResultSet> results = new ArrayList<ResultSet>();

        if (node.predicateType.equals(And.class)) {
            while (it.hasNext()) {
                QueryParserNode n = it.next();
                if (n.predicateType.equals(And.class)) break;
                else results.add(performQuery(n));
            }
        } else if (node.predicateType.equals(Similar.class)) {
            //get the respective searcher
            Searcher s = searcherMap.get(node.field);
            //creating a simpler CriteriaQuery only with Similar desired
            CriteriaQueryImpl criteriaImpl = new CriteriaQueryImpl(resultClass);
            CriteriaQuery newQuery = criteriaImpl.where(node.predicate);
            return s.search(newQuery);
        }

        //TO DO - remove this from here, because this is always making an AND operation
        return combiner.combine(results);
    }
}