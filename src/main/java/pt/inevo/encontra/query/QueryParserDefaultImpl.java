package pt.inevo.encontra.query;

import java.util.Iterator;
import java.util.Stack;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.criteria.ExpressionVisitor;
import pt.inevo.encontra.query.criteria.exps.And;
import pt.inevo.encontra.query.criteria.exps.Constant;
import pt.inevo.encontra.query.criteria.exps.Equal;
import pt.inevo.encontra.query.criteria.exps.Similar;

/**
 * Default implementation of an internal parser for the query
 * @author Ricardo
 */
public class QueryParserDefaultImpl extends ExpressionVisitor.AbstractVisitor implements QueryParser {

    protected Stack<QueryParserNode> subqueries = new Stack<QueryParserNode>();
    protected Iterator<QueryParserNode> it;

    @Override
    public void enter(Expression expr) {
        if (expr instanceof And) {
            QueryParserNode and = new QueryParserNode();
            and.predicateType = And.class;
            and.predicate = expr;
            subqueries.add(and);
        } else if (expr instanceof Similar) {
            QueryParserNode similar = new QueryParserNode();
            similar.predicateType = Similar.class;
            similar.predicate = expr;
            subqueries.add(similar);
        } else if (expr instanceof Constant) {
            Constant t = (Constant) expr;
            QueryParserNode lastNode = subqueries.lastElement();
            lastNode.fieldObject = new IndexedObject(null, t.arg);
        } else if (expr instanceof Path) {
            if (!subqueries.empty()) {
                Path p = (Path) expr;
                String fieldName = p.getAttributeName();
                QueryParserNode lastNode = subqueries.lastElement();
                lastNode.field = fieldName;
            }
        } else if (expr instanceof Equal) {
            //TO DO - implement here the equal operation parser
        } else {
            System.out.println(expr.toString());
            //don't know what else to do
        }
    }

    @Override
    public Stack<QueryParserNode> parse(Query query) {
        if (query instanceof CriteriaQuery) {
            CriteriaQuery q = (CriteriaQuery)query;
            q.getRestriction().acceptVisit(this);
            return subqueries;
        }

        return subqueries;
    }
}
