package pt.inevo.encontra.query.criteria;

import pt.inevo.encontra.query.Expressions;
import pt.inevo.encontra.query.Order;
import pt.inevo.encontra.query.Predicate;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.criteria.exps.And;

import java.util.ArrayList;
import java.util.List;
import pt.inevo.encontra.query.Path;
import pt.inevo.encontra.query.Query;

/**
 * Criteria query implementation.
 *
 * Collects clauses of criteria query (e.g. select projections, from/join,
 * where conditions, order by).
 * Eventually translates these clauses to a similar form of Expression tree
 * that can be interpreted and executed against a data store by OpenJPA kernel.
 *
 * based on openjpa-persistence/src/main/java/org/apache/openjpa/persistence/criteria/CriteriaQueryImpl.java
 */
public class CriteriaQueryImpl<T> implements CriteriaQuery<T>, Query {
    private final Class<T>  _resultClass;
    private PredicateImpl _where;
    private boolean  _distinct;
    private int _limit;      //TO DO should be removed - here now just to keep coding
    private List<Order> _orders;
    private StorageCriteria criteria;

    public CriteriaQueryImpl(Class<T> resultClass) {
        this._resultClass = resultClass;
    }

    /**
     * Sets whether this query as distinct.
     */
    @Override
    public CriteriaQuery<T> distinct(boolean distinct) {
        _distinct = distinct;
        return this;
    }

    @Override
    public CriteriaQuery<T> setCriteria(StorageCriteria criteria){
        this.criteria = criteria;
        return this;
    }

    @Override
    public StorageCriteria getCriteria(){
        return criteria;
    }

    @Override
    public CriteriaQuery<T> limit(int value){
        _limit = value;
        return this;
    }

    @Override
    public CriteriaQuery<T> where(Expression<Boolean> restriction) {
        if (restriction == null) {
            _where = null;
            return this;
        }
        _where = (PredicateImpl)restriction;
        return this;
    }

    @Override
    public CriteriaQuery<T> where(Predicate... restrictions) {
        if (restrictions == null) {
            _where = null;
            return this;
        }
        _where = new And(restrictions);
        return this;
    }

    @Override
    public CriteriaQuery<T> orderBy(Order... orders) {
        if (orders == null) {
            _orders = null;
            return this;
        }
        _orders = new ArrayList<Order>();
        for (Order o : orders) {
            _orders.add(o);
        }
        return this;
    }

    /**
     * Gets the list of ordering elements.
     *
     * @return Empty list if there is no ordering elements.
     * The returned list if mutable but mutation has no impact on this query.
     */
    public List<Order> getOrderList() {
        return Expressions.returnCopy(_orders);
    }
    
    @Override
    public Expression<Boolean> getRestriction() {
        return _where;
    }

    @Override
    public Predicate getRestrictions(){
        return _where;
    }

    @Override
    public Class<T> getResultType() {
        return _resultClass;
    }

    /**
     * Affirms if selection of this query is distinct.
     */
    @Override
    public boolean isDistinct() {
        return _distinct;
    }

    @Override
    public int getLimit(){
        return _limit;
    }

    /**
     * The OpenJPA version returns a Root<X> which extends a Path
     * @param cls
     * @param <X>
     * @return
     */
    @Override
    public <X> Path<X> from(Class<X> cls) {
        return new Path<X>(cls);
    }
}
