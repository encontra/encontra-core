package pt.inevo.encontra.query.criteria.exps;

import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.criteria.ExpressionVisitor;
import pt.inevo.encontra.query.Predicate;
import pt.inevo.encontra.query.criteria.PredicateImpl;

import java.util.Collection;

/**
 * Concrete AND predicate.
 *
 */
public  class And extends PredicateImpl {
    public And(Expression<Boolean> x, Expression<Boolean> y) {
        super(Predicate.BooleanOperator.AND);
        add(x).add(y);
    }

    public And(Predicate...restrictions) {
        super(Predicate.BooleanOperator.AND, restrictions);
    }

    /*
    @Override
    protected boolean eval(Object candidate, Object orig,Object[] params) {
        return _exp1.evaluate(candidate, orig, params)
                && _exp2.evaluate(candidate, orig, params);
    }

    protected boolean eval(Collection candidates, StoreContext ctx,
                           Object[] params) {
        return _exp1.evaluate(candidates, ctx, params)
                && _exp2.evaluate(candidates, ctx, params);
    }
    */
    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        for(Predicate p : _exps){
            p.acceptVisit(visitor);
        }
        //_exp1.acceptVisit(visitor);
        //_exp2.acceptVisit(visitor);
        visitor.exit(this);
    }
}
