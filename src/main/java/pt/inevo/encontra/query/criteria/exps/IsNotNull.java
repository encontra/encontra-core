package pt.inevo.encontra.query.criteria.exps;

import pt.inevo.encontra.query.criteria.ExpressionVisitor;
import pt.inevo.encontra.query.criteria.*;

public class IsNotNull extends PredicateImpl {
    final ExpressionImpl<?> e;
    public IsNotNull(ExpressionImpl<?> e) {
        super();
        this.e = e;
    }

    @Override
    public PredicateImpl not() {
        return new IsNull(e).markNegated();
    }

    @Override
    public void acceptVisit(ExpressionVisitor visitor) {
        super.acceptVisit(visitor);
        Expressions.acceptVisit(visitor, e);
    }
    
}
