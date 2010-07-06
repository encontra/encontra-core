package pt.inevo.encontra.query;

import pt.inevo.encontra.index.AbstractObject;
import pt.inevo.encontra.query.Query;

/**
 * K neighrest neighbor Query - Retrieve the K most similar elements
 * @author ricardo
 */
public class KnnQuery extends Query {

    protected AbstractObject queryObject;
    protected int knn;

    public KnnQuery(){}

    public KnnQuery(AbstractObject queryObject, int k){
        this.queryObject = queryObject;
        this.knn = k;
    }

    /**
     * Obtains the object to be used as query
     * @return the query object of the KNNQuery
     */
    public AbstractObject getQueryObject() {
        return queryObject;
    }

    /**
     * Sets the object do be used as the query for KNN algorithm
     * @param queryObject the object to be used as query
     */
    public void setQueryObject(AbstractObject queryObject) {
        this.queryObject = queryObject;
    }

    /**
     * Obtains the number of the desired similar elements.
     * @return the knn
     */
    public int getKnn() {
        return knn;
    }

    /**
     * Sets the number of similar elements this query must return
     * @param knn
     */
    public void setKnn(int knn) {
        this.knn = knn;
    }

    @Override
    public QueryType getType() {
        return QueryType.KNN;
    }
}
