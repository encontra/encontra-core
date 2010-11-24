package pt.inevo.encontra.query;

import java.util.Stack;
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.criteria.ExpressionVisitor;
import pt.inevo.encontra.query.criteria.exps.And;
import pt.inevo.encontra.query.criteria.exps.Constant;
import pt.inevo.encontra.query.criteria.exps.Equal;
import pt.inevo.encontra.query.criteria.exps.Or;
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
        if (expr instanceof And) {  //creating a And node
            QueryParserNode and = new QueryParserNode();
            and.predicateType = And.class;
            and.predicate = expr;

            //push the object into the stack
            pile.push(and);

            if (currentTopNode != null && (currentTopNode.predicateType.equals(And.class)
                    || currentTopNode.predicateType.equals(Or.class))) {
                currentTopNode.childrenNodes.add(and);
            }

            currentTopNode = and;

        } else if (expr instanceof Or) {    //creating a Or node
            QueryParserNode or = new QueryParserNode();
            or.predicateType = Or.class;
            or.predicate = expr;

            //push the object into the stack
            pile.push(or);

            if (currentTopNode != null && (currentTopNode.predicateType.equals(And.class)
                    || currentTopNode.predicateType.equals(Or.class))) {
                currentTopNode.childrenNodes.add(or);
            }

            currentTopNode = or;

        } else if (expr instanceof Similar) {
            QueryParserNode similar = new QueryParserNode();
            similar.predicateType = Similar.class;
            similar.predicate = expr;

            if (currentTopNode != null) {
                if (currentTopNode.predicateType.equals(And.class)
                        || currentTopNode.predicateType.equals(Or.class)) {
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

            Equal eq = (Equal)expr;
            QueryParserNode equalNode = new QueryParserNode();
            equalNode.predicate = eq;
            equalNode.predicateType = Equal.class;

            if (currentTopNode != null) {
                if (currentTopNode.predicateType.equals(And.class)
                        || currentTopNode.predicateType.equals(Or.class)) {
                    currentTopNode.childrenNodes.add(equalNode);
                } else {
                    currentTopNode = pile.pop();
                    currentTopNode.childrenNodes.add(equalNode);
                    pile.push(currentTopNode);
                }
            }

            currentTopNode = equalNode;
            
        } else {
            //don't know what else to do
            System.out.println(expr.toString());
        }
    }

    @Override
    public void exit(Expression expr) {
        super.exit(expr);
        if (expr instanceof And || expr instanceof Or) {
            if (!pile.empty()) {
                currentTopNode = pile.pop();     //remove the node because it is no longer necessary
                if (!pile.empty()) {
                    currentTopNode = pile.peek();    //get the next element in the tree
                }
            }
        }
    }

    private void resetParser() {
        pile = new Stack<QueryParserNode>();
        currentTopNode = null;
    }

    @Override
    public QueryParserNode parse(Query query) {
        if (query instanceof CriteriaQuery) {
            resetParser();
            CriteriaQuery q = (CriteriaQuery) query;
            q.getRestriction().acceptVisit(this);
            return currentTopNode;
        }

        return currentTopNode;
    }
}
