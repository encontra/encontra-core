package pt.inevo.encontra.query.criteria.exps;

import pt.inevo.encontra.query.criteria.*;

/**
 * Binary Logical Expression applies a function on a pair of input Expression to generate a Predicate
 * i.e. an expression whose resultant type is Boolean.
 *
 */
public abstract class BinaryLogicalExpression extends PredicateImpl {
    protected final ExpressionImpl<?> e1;
    protected final ExpressionImpl<?> e2;

    public BinaryLogicalExpression(Expression<?> x, Expression<?> y) {
        super();
        e1 = (ExpressionImpl<?>)x;
        e2 = (ExpressionImpl<?>)y;
    }


    public void acceptVisit(ExpressionVisitor visitor) {
        Expressions.acceptVisit(visitor, this, e1, e2);
    }
}
