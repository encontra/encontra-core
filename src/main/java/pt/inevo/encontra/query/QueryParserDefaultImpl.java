package pt.inevo.encontra.query;

import java.util.Stack;
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

    protected Stack<QueryParserNode> pile = new Stack<QueryParserNode>();
    protected QueryParserNode currentTopNode = null;

    @Override
    public void enter(Expression expr) {
        if (expr instanceof And) {
            QueryParserNode and = new QueryParserNode();
            and.predicateType = And.class;
            and.predicate = expr;

            //push the object into the stack
            pile.push(and);

            currentTopNode = and;

        } else if (expr instanceof Similar) {
            QueryParserNode similar = new QueryParserNode();
            similar.predicateType = Similar.class;
            similar.predicate = expr;

            if (currentTopNode != null) {
                if (currentTopNode.predicateType.equals(And.class)) {
                    currentTopNode.childrenNodes.add(similar);
                } else {
                    currentTopNode = pile.pop();
                    currentTopNode.childrenNodes.add(similar);
                    pile.push(currentTopNode);
                }
                
            }

            currentTopNode = similar;

        } else if (expr instanceof Constant) {
            Constant t = (Constant) expr;
            QueryParserNode constantNode = new QueryParserNode();
            constantNode.fieldObject = t.arg;
            constantNode.predicate = t;
            constantNode.predicateType = Constant.class;
            constantNode.fieldObject = t.arg;

            currentTopNode.fieldObject = t.arg;
            currentTopNode.childrenNodes.add(constantNode);

        } else if (expr instanceof Path) {

            Path p = (Path) expr;
            QueryParserNode pathNode = new QueryParserNode();
            pathNode.predicateType = Path.class;
            pathNode.predicate = p;

            if (p.isField()) {
                String fieldName = p.getAttributeName();
                currentTopNode.field = fieldName;
            }

            currentTopNode.childrenNodes.add(pathNode);
        } else if (expr instanceof Equal) {
            // TODO implement here the Equal parsing
        } else {
            //don't know what else to do
            System.out.println(expr.toString());
        }
    }

    @Override
    public void exit(Expression expr) {
        super.exit(expr);
        if (expr instanceof And) {
            currentTopNode = pile.pop();
        }
    }

    @Override
    public QueryParserNode parse(Query query) {
        if (query instanceof CriteriaQuery) {
            CriteriaQuery q = (CriteriaQuery) query;
            q.getRestriction().acceptVisit(this);
            return currentTopNode;
        }

        return currentTopNode;
    }
}