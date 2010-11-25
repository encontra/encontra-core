package pt.inevo.encontra.query.criteria;

import pt.inevo.encontra.query.Expressions;
import pt.inevo.encontra.query.ExpressionImpl;
import pt.inevo.encontra.query.Predicate;
import pt.inevo.encontra.query.criteria.exps.Not;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Predicate is a expression that evaluates to true or false.
 * All boolean expressions are implemented as Predicate.
 * A predicate can have zero or more predicate arguments.
 * Default predicate operator is AND (conjunction).
 * Two constant predicates are Predicate.TRUE and Predicate.FALSE.
 * AND predicate with no argument evaluates to TRUE.
 * OR predicate with no argument evaluates to FALSE.
 * Negation of a Predicate creates a new Predicate.**/
abstract public class PredicateImpl extends ExpressionImpl<Boolean> implements Predicate {

    protected final List<Predicate> _exps = new ArrayList<Predicate>();
    private final BooleanOperator _op;
    private boolean _negated = false;

    /**
     * An AND predicate with no arguments.
     */
    protected PredicateImpl() {
        this(BooleanOperator.AND);
    }

    /**
     * A predicate with the given operator.
     */
    protected PredicateImpl(BooleanOperator op) {
        super(Boolean.class);
        _op = op;
    }

    /**
     * A predicate of given operator with given arguments.
     */
    protected PredicateImpl(BooleanOperator op, Predicate...restrictions) {
        this(op);
        if (restrictions != null) {
            for (Predicate p : restrictions)
                add(p);
        }
    }

    /**
     * Adds the given predicate expression.
     */
    public PredicateImpl add(Expression<Boolean> s) {
        _exps.add((Predicate)s); // all boolean expressions are Predicate
        return this;
    }

    public List<Expression<Boolean>> getExpressions() {
        List<Expression<Boolean>> result = new CopyOnWriteArrayList<Expression<Boolean>>();
        if (_exps.isEmpty())
            return result;
        result.addAll(_exps);
        return result;
    }

    public final BooleanOperator getOperator() {
        return _op;
    }

    public final boolean isEmpty() {
        return _exps.isEmpty();
    }

    /**
     * Is this predicate created by negating another predicate?
     */
    public final boolean isNegated() {
        return _negated;
    }

    /**
     * Returns a new predicate as the negation of this predicate.
     * <br>
     * Note:
     * Default negation creates a Not expression with this receiver as delegate.
     * Derived predicates can return the inverse expression, if exists.
     * For example, NotEqual for Equal or LessThan for GreaterThanEqual etc.
     */
    public PredicateImpl not() {
        return new Not(this).markNegated();
    }

    public PredicateImpl markNegated() {
        _negated = true;
        return this;
    }

    @Override
    public void acceptVisit(ExpressionVisitor visitor) {
        Expressions.acceptVisit(visitor, this, _exps.toArray(new Expression<?>[_exps.size()]));
    }
}