package pt.inevo.encontra.query.criteria.exps;

import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.criteria.ExpressionImpl;
import pt.inevo.encontra.query.criteria.ExpressionVisitor;
import pt.inevo.encontra.query.criteria.Expressions;

/**
 * Binary Functional Expression applies a binary function on a pair of input Expression.
 *
 * @param <X> the type of the resultant expression
 */
public abstract class BinaryFunctionalExpression<X> extends ExpressionImpl<X>{
    protected final ExpressionImpl<?> e1;
    protected final ExpressionImpl<?> e2;

    /**
     * Supply the resultant type and pair of input operand expressions.
     */
    public BinaryFunctionalExpression(Class<X> t, Expression<?> x, Expression<?> y) {
        super(t);
        e1 = (ExpressionImpl<?>)x;
        e2 = (ExpressionImpl<?>)y;
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        Expressions.acceptVisit(visitor, this, e1, e2);
    }
}
