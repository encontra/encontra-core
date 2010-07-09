package pt.inevo.encontra.query;

import pt.inevo.encontra.descriptors.Descriptor;

/**
 * K neighrest neighbor Query - Retrieve the K most similar elements
 * @author ricardo
 */
public class KnnQuery extends Query {

    protected Object query;
    protected int knn;

    public KnnQuery(){}

    public KnnQuery(Object query, int k){
        this.query = query;
        this.knn = k;
        super.type = QueryType.KNN;
    }

    /**
     * Obtains the object to be used as query
     * @return the query object of the KNNQuery
     */
    public Object getQuery() {
        return query;
    }

    /**
     * Sets the object do be used as the query for KNN algorithm
     * @param queryObject the object to be used as query
     */
    public void setQuery(Object queryObject) {
        this.query = queryObject;
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
}
