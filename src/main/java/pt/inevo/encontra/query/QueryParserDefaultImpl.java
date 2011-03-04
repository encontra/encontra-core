package pt.inevo.encontra.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import pt.inevo.encontra.query.criteria.*;
import pt.inevo.encontra.query.criteria.exps.*;

/**
 * Default implementation of an internal parser for the CriteriaQuery.
 * @author Ricardo
 */
public class QueryParserDefaultImpl extends ExpressionVisitor.AbstractVisitor implements QueryParser {

    protected Stack<QueryParserNode> pile = new Stack<QueryParserNode>();
    protected Stack<QueryParserNode> negatedPile = new Stack<QueryParserNode>();
    protected QueryParserNode currentTopNode;
    protected QueryParserNode negatedParentNode;
    protected Map<Class, Class> operatorsNegation = new HashMap<Class, Class>();
    protected boolean negated = false;
    protected boolean distinct;
    protected int limit;
    protected StorageCriteria criteria;

    public QueryParserDefaultImpl() {
        super();
        operatorsNegation.put(Or.class, And.class);
        operatorsNegation.put(And.class, Or.class);
        operatorsNegation.put(Equal.class, NotEqual.class);
        operatorsNegation.put(NotEqual.class, Equal.class);
        operatorsNegation.put(Similar.class, NotEqual.class);
    }

    @Override
    public QueryParserNode parse(Query query) {
        if (query instanceof CriteriaQuery) {
            resetParser();
            CriteriaQueryImpl q = (CriteriaQueryImpl) query;
            distinct = q.isDistinct();
            limit = q.getLimit();
            criteria = q.getCriteria();

            q.getRestriction().acceptVisit(this);

            currentTopNode.criteria = criteria;
            return currentTopNode;
        } else {
            //just for having an empty node
            currentTopNode = new QueryParserNode();
            return currentTopNode;
        }
    }

    @Override
    public void enter(Expression expr) {
        if (expr instanceof And) {  //creating a And node
            processBooleanOperator(expr);
        } else if (expr instanceof Or) {    //creating a Or node
            processBooleanOperator(expr);
        } else if (expr instanceof Similar) {
            processSimilarEquals(expr);
        } else if (expr instanceof Constant) {
            //creating the constant node
            Constant t = (Constant) expr;
            QueryParserNode constantNode = new QueryParserNode();
            constantNode.fieldObject = t.arg;
            constantNode.predicate = t;
            constantNode.predicateType = Constant.class;
            constantNode.fieldObject = t.arg;
            currentTopNode.fieldObject = t.arg;
            currentTopNode.childrenNodes.add(constantNode);
        } else if (expr instanceof Path) {
            //creating the Path node
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
            processSimilarEquals(expr);
        } else if (expr instanceof NotEqual) {
            processSimilarEquals(expr);
        } else if (expr instanceof Not) {
            negated = true;
            negatedParentNode = null;
        } else {
            //don't know what else to do
            System.out.println("[Error]: QueryParserDefaultImpl: cannot recognize "
                    + "the expression ->" +expr.toString());
        }
    }

    @Override
    public void exit(Expression expr) {
        super.exit(expr);
        //going to another branch of the query
        if (expr instanceof And || expr instanceof Or) {
            if (!pile.empty()) {
                currentTopNode = pile.pop();     //remove the node because it is no longer necessary
                
                if (!negatedPile.isEmpty() && negatedPile.peek().equals(currentTopNode)) {
                    negatedParentNode = currentTopNode;
                }
                if (!pile.empty()) {
                    currentTopNode = pile.peek();    //get the next element in the tree
                    if (!negatedPile.isEmpty() && negatedPile.peek().equals(currentTopNode)) {
                        negatedParentNode = currentTopNode;
                    }
                }
            }
        }
    }

        /**
     * Processes the And and Or predicates.
     * @param expr
     * @return
     */
    private void processBooleanOperator(Expression expr) {

        QueryParserNode node = new QueryParserNode();
        PredicateImpl predicate = (PredicateImpl) expr;
        node.distinct = distinct;
        node.limit = limit;

        if (predicate.isNegated() || negated || negatedParentNode != null) {
            node.predicateType = operatorsNegation.get(expr.getClass());
            node.predicate = predicate.markNegated();
            negatedPile.push(node);

            negatedParentNode = node;
            negated = false;
        } else {
            node.predicateType = expr.getClass();
            node.predicate = predicate;
        }

        //push the object into the stack
        pile.push(node);

        if (currentTopNode != null && (currentTopNode.predicateType.equals(And.class)
                || currentTopNode.predicateType.equals(Or.class))) {
            currentTopNode.childrenNodes.add(node);
        }

        currentTopNode = node;
    }

    /**
     * Processes the Similar, Equal and NotEqual expressions.
     * @param expr
     */
    private void processSimilarEquals(Expression expr) {

        QueryParserNode node = new QueryParserNode();
        PredicateImpl predicate = (PredicateImpl) expr;
        node.predicateType = expr.getClass();
        node.predicate = expr;
        node.distinct = distinct;
        node.limit = limit;

        if (currentTopNode != null) {
            if (currentTopNode.predicateType.equals(And.class)
                    || currentTopNode.predicateType.equals(Or.class)) {
                currentTopNode.childrenNodes.add(node);
            } else {
                currentTopNode = pile.pop();
                currentTopNode.childrenNodes.add(node);
                pile.push(currentTopNode);
            }
        }

        currentTopNode = node;

        if (negatedParentNode != null) {
            if (negatedParentNode.predicateType.equals(And.class)
                    || negatedParentNode.predicateType.equals(Or.class)) {
                node.predicateType = operatorsNegation.get(expr.getClass());
                node.predicate = predicate.markNegated();
            }
        } else if (negated || (predicate.isNegated() && !predicate.getClass().equals(NotEqual.class))) {
            node.predicateType = operatorsNegation.get(expr.getClass());
            negated = false;
        }
    }

    /**
     * Resets the parser.
     */
    private void resetParser() {
        pile = new Stack<QueryParserNode>();
        currentTopNode = null;
        negated = false;
        negatedParentNode = null;
        distinct = false;
    }
}
