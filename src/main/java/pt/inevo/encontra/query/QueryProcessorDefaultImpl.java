package pt.inevo.encontra.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import pt.inevo.encontra.engine.QueryProcessor;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.ResultSet;
import pt.inevo.encontra.index.search.Searcher;
import pt.inevo.encontra.query.criteria.CriteriaQuery;
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.criteria.Path;
import pt.inevo.encontra.query.criteria.exps.And;
import pt.inevo.encontra.query.criteria.exps.Constant;
import pt.inevo.encontra.query.criteria.exps.Equal;
import pt.inevo.encontra.query.criteria.exps.Similar;

class QueryTreeNode {

    public QueryTreeNode nextNode;
    public Class operationClazz;
    public String field;
    public IndexedObject fieldObject;
}

/**
 *
 * @author Ricardo
 */
public class QueryProcessorDefaultImpl<E extends IndexedObject> extends QueryProcessor<E> {

    protected final Set<Expression> _visited = new HashSet<Expression>();
    Stack<QueryTreeNode> subqueries = new Stack<QueryTreeNode>();
    QueryTreeNode queryRoot = new QueryTreeNode();
    Iterator<QueryTreeNode> it;

    public QueryProcessorDefaultImpl() {
        super();
        combiner = new SimpleQueryCombiner();
    }

    @Override
    public ResultSet search(Query query) {
        CriteriaQuery q = (CriteriaQuery) query;
        q.getRestriction().acceptVisit(this);

        it = subqueries.iterator();
        return searcher(it.next());
    }

    ResultSet searcher(QueryTreeNode node){
        
        List<ResultSet> results = new ArrayList<ResultSet>();

        if (node.operationClazz.equals(And.class)) {
            while (it.hasNext()) {
                QueryTreeNode n = it.next();
                if (n.operationClazz.equals(And.class)) break;
                else results.add(searcher(n));
            }
        } else if (node.operationClazz.equals(Similar.class)) {
            Searcher s = searcherMap.get(node.field);
            return s.search(new KnnQuery(node.fieldObject, 10));
        }
        return combiner.combine(results);
    }

    @Override
    public void enter(Expression expr) {
        
        if (expr instanceof And) {
            //TO DO - implements the call to And
            System.out.println("AND");
            QueryTreeNode and = new QueryTreeNode();
            and.operationClazz = And.class;
            subqueries.add(and);
        } else if (expr instanceof Similar) {
            //TO DO - implements the call to Similar
            System.out.println("SIMILAR");
            QueryTreeNode similar = new QueryTreeNode();
            similar.operationClazz = Similar.class;
            subqueries.add(similar);
        } else if (expr instanceof Equal) {
            System.out.println("EQUAL");
            //TO DO - implement the call to equals
        } else if (expr instanceof Constant) {
            System.out.println("CONSTANT");
            Constant t = (Constant)expr;
            QueryTreeNode lastNode = subqueries.lastElement();
            lastNode.fieldObject = new IndexedObject(null, t.arg);
        } else if (expr instanceof Path) {
            System.out.println("PATH");
            if (!subqueries.empty()) {
                Path p = (Path)expr;
                String fieldName = p.getAttributeName();
                QueryTreeNode lastNode = subqueries.lastElement();
                lastNode.field = fieldName;
            }      
        } else {
            System.out.println(expr.toString());
        }
    }

    /**
     * Remembers the node being visited.
     */
    @Override
    public void exit(Expression expr) {
        _visited.add(expr);
    }

    /**
     * Affirms if this node has been visited before.
     */
    @Override
    public boolean isVisited(Expression expr) {
        return _visited.contains(expr);
    }

    /**
     * Returns PREFIX as the default traversal style.
     */
    @Override
    public TraversalStyle getTraversalStyle(Expression expr) {
        return TraversalStyle.PREFIX;
    }
}
