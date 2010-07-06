/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.inevo.encontra.query;

import pt.inevo.encontra.query.Query;


/**
 * Criteria Query - Query for combining Criteria
 * @author ricardo
 */
public class CriteriaQuery extends Query {

    @Override
    public QueryType getType() {
        return QueryType.CRITERIA;
    }
}
