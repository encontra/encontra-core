package pt.inevo.encontra.query.criteria.exps;

import pt.inevo.encontra.query.Expressions;
import pt.inevo.encontra.query.ExpressionImpl;
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.criteria.ExpressionVisitor;
import pt.inevo.encontra.query.criteria.*;

public  class Not extends PredicateImpl {
    protected final ExpressionImpl<Boolean> e;
    public Not(Expression<Boolean> ne) {
        super();
        e = (ExpressionImpl<Boolean>)ne;
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        Expressions.acceptVisit(visitor, this, e);
    }

}