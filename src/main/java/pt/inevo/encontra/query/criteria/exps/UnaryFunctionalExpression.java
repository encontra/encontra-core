package pt.inevo.encontra.query.criteria.exps;

import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.ExpressionImpl;
import pt.inevo.encontra.query.criteria.ExpressionVisitor;
import pt.inevo.encontra.query.Expressions;

/**
 * Unary Functional Expression applies a unary function on a input operand Expression.
 *
 * @param <X> the type of the resultant expression
 */
public abstract class UnaryFunctionalExpression<X> extends ExpressionImpl<X> {
    protected final ExpressionImpl<?> e;
    /**
     * Supply the resultant type and input operand expression.
     */
    public UnaryFunctionalExpression(Class<X> t, Expression<?> e) {
        super(t);
        this.e  = (ExpressionImpl<?>)e;
    }

//    public UnaryFunctionalExpression(Expression<X> e) {
//        this((Class<X>)e.getJavaType(), e);
//    }

    public void acceptVisit(ExpressionVisitor visitor) {
        Expressions.acceptVisit(visitor, this, e);
    }
}
