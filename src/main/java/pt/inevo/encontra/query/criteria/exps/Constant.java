package pt.inevo.encontra.query.criteria.exps;

import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.criteria.ExpressionImpl;
import pt.inevo.encontra.query.criteria.ExpressionVisitor;
import pt.inevo.encontra.query.criteria.Expressions;


public class Constant<X> extends ExpressionImpl<X> {
    public final Object arg;
    public Constant(Class<X> t, X x) {
        super(t);
        this.arg = x;
    }

    public Constant(X x) {
        this(x == null ? null : (Class<X>)x.getClass(), x);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        Expressions.acceptVisit(visitor, this, arg instanceof Expression ? ((Expression)arg) : null);
    }
}
