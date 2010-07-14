package pt.inevo.encontra.query;

import pt.inevo.encontra.descriptors.Descriptor;

/**
 * Approximate K Nearest Neighbour Query - uses stop conditions to accelerate
 * the Knn Query
 * @author ricardo
 */
public class AproximateKnnQuery extends KnnQuery {

    public AproximateKnnQuery(Descriptor query, int knn){
        super(query, knn);
        super.type = QueryType.APROXIMATE_KNN;
    }
}
