package pt.inevo.encontra.query.criteria;


/**
 * based on openjpa-persistence/src/main/java/org/apache/openjpa/persistence/criteria/OrderImpl.java
 */
public class Order implements Expression{
    private boolean _ascending;
    private final ExpressionImpl<?> e;

    public Order(Expression<?> e, boolean asc) {
        this.e = (ExpressionImpl<?>) e;
        _ascending = asc;
    }

    public Order(Expression<?> e) {
        this(e, true);
    }

    public ExpressionImpl<?> getExpression() {
        return e;
    }

    public boolean isAscending() {
        return _ascending;
    }

    public Order reverse() {
        _ascending = !_ascending;
        return this;
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        if (!visitor.isVisited(this)) {
            visitor.enter(this);
            visitor.exit(this);
        }
    }
}
