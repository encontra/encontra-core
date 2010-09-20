package pt.inevo.encontra.query.criteria.exps;

import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.criteria.PredicateImpl;

/**
 * from openjpa-persistence/src/main/java/org/apache/openjpa/persistence/criteria/Expressions.java
 */
public class Equal extends BinaryLogicalExpression {
    public <X,Y> Equal(Expression<X> x, Expression<Y> y) {
        super(x,y);
    }

    public <X> Equal(Expression<X> x, Object y) {
        this(x, new Constant(y));
    }

    @Override
    public PredicateImpl not() {
        return new NotEqual(e1, e2).markNegated();
    }    
}
