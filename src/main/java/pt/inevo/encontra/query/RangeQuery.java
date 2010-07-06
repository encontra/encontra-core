package pt.inevo.encontra.query;

import pt.inevo.encontra.index.AbstractObject;
import pt.inevo.encontra.query.Query;

/**
 * Range Query. Given a query (seed point) performs a range query using the
 * maximum range passed as a parameter
 * @author ricardo
 */
public class RangeQuery extends Query {

    protected AbstractObject queryObject;
    protected double range;

    public RangeQuery(){}

    public RangeQuery(AbstractObject query, double range){
        this.queryObject = query;
        this.range = range;
    }

    /**
     * Obtains the object used as seed (query) to the range query
     * @return the queryObject
     */
    public AbstractObject getQueryObject() {
        return queryObject;
    }

    /**
     * Sets the seed of the Range Query
     * @param queryObject the query object of the RangeQuery
     */
    public void setQueryObject(AbstractObject queryObject) {
        this.queryObject = queryObject;
    }

    /**
     * Obtains the specified range of the Range query
     * @return the range
     */
    public double getRange() {
        return range;
    }

    /**
     * Sets the seed of the Range Query
     * @param range the range to set
     */
    public void setRange(double range) {
        this.range = range;
    }

    @Override
    public QueryType getType() {
        return QueryType.RANGE;
    }
}
