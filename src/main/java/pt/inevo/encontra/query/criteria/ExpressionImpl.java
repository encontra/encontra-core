package pt.inevo.encontra.query.criteria;

import java.util.Collection;

/**
 * Expression node for Criteria query.
 * Acts a bridge pattern to equivalent kernel representation.
 *
 * @param <X> the type of the value this expression represents.
 **/
public class ExpressionImpl<X> implements Expression<X> {

    private final Class<X> _cls;

    /**
     * @param cls the type of the evaluated result of the expression
     */
    public ExpressionImpl(Class<X> cls) {
        _cls = cls;
    }

    /**
     * Gets the immutable type represented by this selection term.
     */
    public Class<X> getJavaType() {
        return _cls;
    }

    @Override
    public void acceptVisit(ExpressionVisitor visitor) {
        Expressions.acceptVisit(visitor, this);
    }

    /**
     * Evaluate the expression for the given candidate.
     */
    public final boolean evaluate(Object candidate, Object orig, Object[] params) {
        try {
            return eval(candidate, candidate, params);
        } catch (ClassCastException cce) {
            return false;
        } catch (NullPointerException npe) {
            return false;
        }
    }

    /**
     * Evaluate the expression for the given candidate group.
     */
    public final boolean evaluate(Collection candidates, Object[] params) {
        try {
            return eval(candidates, params);
        } catch (ClassCastException cce) {
            return false;
        } catch (NullPointerException npe) {
            return false;
        }
    }

    /**
     * Evaluate the expression for the given context candidate and original
     * candidate.
     */
    protected boolean eval(Object candidate, Object orig, Object[] params) {
        return true;
    }

    /**
     * Evaluate the expression for the given group.
     */
    protected boolean eval(Collection candidates, Object[] params) {
        return true;
    }
}
