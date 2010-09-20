package pt.inevo.encontra.query.criteria.exps;

import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.criteria.PredicateImpl;

public class NotEqual extends BinaryLogicalExpression {
    public <X,Y> NotEqual(Expression<X> x, Expression<Y> y) {
        super(x,y);
    }

    public <X> NotEqual(Expression<X> x, Object y) {
        this(x, new Constant(y));
    }

    @Override
    public PredicateImpl not() {
        return new Equal(e1, e2).markNegated();
    }

}