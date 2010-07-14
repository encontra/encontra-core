package pt.inevo.encontra.query.criteria;

import pt.inevo.encontra.query.Query;

/**
 * Criteria Query - Query for combining Criteria
 * @author ricardo
 */
public class CriteriaQuery extends Query {

    public CriteriaQuery(){
        super.type = QueryType.CRITERIA;
    }
}
