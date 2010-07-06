package pt.inevo.encontra.query;

import pt.inevo.encontra.query.Query;

/**
 * Random Query - A query to retrieve random objects
 * @author ricardo
 */
public class RandomQuery extends Query {

    @Override
    public QueryType getType() {
        return QueryType.RANDOM;
    }

}
