package pt.inevo.encontra.query.criteria.exps;

import pt.inevo.encontra.query.criteria.ExpressionVisitor;
import pt.inevo.encontra.query.criteria.*;

/**
 * from openjpa-persistence/src/main/java/org/apache/openjpa/persistence/criteria/Expressions.java
 */
public class IsNull extends PredicateImpl {
    final ExpressionImpl<?> e;
    public IsNull(ExpressionImpl<?> e) {
        super();
        this.e = e;
    }

    @Override
    public PredicateImpl not() {
        return new IsNotNull(e).markNegated();
    }

    @Override
    public void acceptVisit(ExpressionVisitor visitor) {
        super.acceptVisit(visitor);
        Expressions.acceptVisit(visitor, e);
    }

}
