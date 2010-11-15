package pt.inevo.encontra.query;

import pt.inevo.encontra.index.IndexedObject;

/**
 *
 * @author Ricardo
 */
public class KnnQuery implements Query {

    protected IndexedObject queryObject;
    protected int knn;

    public KnnQuery(IndexedObject o, int knn){
        this.queryObject = o;
        this.knn = knn;
    }

    /**
     * @return the queryObject
     */
    public IndexedObject getQueryObject() {
        return queryObject;
    }

    /**
     * @return the knn
     */
    public int getKnn() {
        return knn;
    }


}
