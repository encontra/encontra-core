package pt.inevo.encontra.query.criteria.exps;

import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.criteria.ExpressionImpl;
import pt.inevo.encontra.query.criteria.ExpressionVisitor;
import pt.inevo.encontra.query.criteria.Expressions;

/**
 * Functional Expression applies a function on a list of input Expressions.
 *
 * @param <X> the type of the resultant expression
 */
public abstract class FunctionalExpression<X> extends ExpressionImpl<X> {
    protected final ExpressionImpl<?>[] args;

    /**
     * Supply the resultant type and list of input operand expressions.
     */
    public FunctionalExpression(Class<X> t, Expression<?>... args) {
        super(t);
        int len = args == null ? 0 : args.length;
        this.args = new ExpressionImpl<?>[len];
        for (int i = 0; args != null && i < args.length; i++) {
            this.args[i] = (ExpressionImpl<?>)args[i];
        }
    }

    @Override
    public void acceptVisit(ExpressionVisitor visitor) {
        Expressions.acceptVisit(visitor, this, args);
    }
}
