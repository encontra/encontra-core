package pt.inevo.encontra.query.criteria.exps;

import pt.inevo.encontra.query.criteria.ExpressionVisitor;
import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.criteria.Predicate;
import pt.inevo.encontra.query.criteria.PredicateImpl;

/**
 * Concrete OR predicate.
 *
 */
public class Or extends PredicateImpl {
    public Or(Expression<Boolean> x, Expression<Boolean> y) {
        super(Predicate.BooleanOperator.OR);
        add(x).add(y);
    }

    public Or(Predicate...restrictions) {
        super(Predicate.BooleanOperator.OR, restrictions);
    }

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
