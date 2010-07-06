package pt.inevo.encontra.query;

import pt.inevo.encontra.index.AbstractObject;

/**
 * Approximate K Nearest Neighbour Query - uses stop conditions to accelerate
 * the Knn Query
 * @author ricardo
 */
public class AproximateKnnQuery extends KnnQuery {

    public AproximateKnnQuery(AbstractObject queryObject, int knn){
        super(queryObject, knn);
    }

    @Override
    public QueryType getType(){
        return QueryType.APROXIMATE_KNN;
    }
}
