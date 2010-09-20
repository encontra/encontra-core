package pt.inevo.encontra.query.criteria.exps;

import pt.inevo.encontra.query.criteria.Expression;
import pt.inevo.encontra.query.criteria.PredicateImpl;

/**
 * from openjpa-persistence/src/main/java/org/apache/openjpa/persistence/criteria/Expressions.java
 */
public class Similar extends BinaryLogicalExpression {
    public <X,Y> Similar(Expression<X> x, Expression<Y> y) {
        super(x,y);
    }

    public <X> Similar(Expression<X> x, Object y) {
        this(x, new Constant(y));
    }

    public <X> Similar(Object x, Object y) {
        this(new Constant(x), new Constant(y));
    }
    
    @Override
    public PredicateImpl not() {
        return new NotEqual(e1, e2).markNegated();
    }    
}
