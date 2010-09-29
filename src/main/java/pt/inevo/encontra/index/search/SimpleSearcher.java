package pt.inevo.encontra.index.search;

import pt.inevo.encontra.descriptors.Descriptor;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.Result;
import pt.inevo.encontra.index.ResultSet;
import pt.inevo.encontra.query.KnnQuery;
import pt.inevo.encontra.query.Query;
import pt.inevo.encontra.query.Query.QueryType;
import pt.inevo.encontra.storage.IEntity;
import pt.inevo.encontra.storage.IEntry;

/**
 *
 */
public class SimpleSearcher<O extends IEntity> extends AbstractSearcher<O> {

    protected DescriptorExtractor extractor;

    public void setDescriptorExtractor(DescriptorExtractor extractor) {
        this.extractor = extractor;
    }

    public DescriptorExtractor getDescriptorExtractor() {
        return extractor;
    }

    @Override
    public boolean insert(O entry) {
        assert (entry != null);
        Descriptor descriptor = extractor.extract(entry);
        return index.insert(descriptor);
    }

    @Override
    public boolean remove(O entry){
        assert (entry != null);
        Descriptor descriptor = extractor.extract(entry);
        return index.remove(descriptor);
    }

    @Override
    public QueryType[] getSupportedQueryTypes() {
        return new QueryType[]{QueryType.KNN, Query.QueryType.RANDOM};
    }

    @Override
    public boolean supportsQueryType(QueryType type) {
        if (type.equals(QueryType.KNN) || type.equals(QueryType.RANDOM)) {
            return true;
        }
        return false;
    }

    @Override
    public ResultSet<O> search(Query query) {
        ResultSet<IEntry> results = new ResultSet<IEntry>();
        if (supportsQueryType(query.getType())) {
            if (query.getType().equals(Query.QueryType.KNN)) {
                KnnQuery q = (KnnQuery) query;
                Descriptor d = getDescriptorExtractor().extract((IndexedObject) q.getQuery());
                results = performKnnQuery(d, q.getKnn());
            } else if (query.getType().equals(Query.QueryType.RANDOM)) {
                results = performRandomQuery();
            }
        }

        return getResultObjects(results);
    }

    private ResultSet<IEntry> performRandomQuery() {

        Descriptor d = index.getFirst();
        return performKnnQuery(d, 10);

    }

    protected ResultSet<IEntry> performKnnQuery(Descriptor d, int maxHits) {
        double overallMaxDistance = 0.0;
        double maxDistance = Double.NEGATIVE_INFINITY;

        ResultSet results = new ResultSet<Descriptor>();

        for ( ; index.hasNext() ; ){
            Descriptor o = index.getNext();

            double distance = d.getDistance(o);
            // calculate the overall max distance to normalize score afterwards
            if (overallMaxDistance < distance) {
                overallMaxDistance = distance;
            }
            // if it is the first document:
            if (maxDistance < 0) {
                maxDistance = distance;
            }
            // if the array is not full yet:
            if (results.size() < maxHits) {
                Result<Descriptor> result = new Result<Descriptor>(o);
                result.setSimilarity(distance); // TODO - This is distance not similarity!!!
                results.add(result);
                if (distance > maxDistance) {
                    maxDistance = distance;
                }
            } else if (distance < maxDistance) {
                // if it is nearer to the sample than at least on of the current set:
                // remove the last one ...
                results.remove(results.size() - 1);
                // add the new one ...
                Result<Descriptor> result = new Result<Descriptor>(o);
                result.setSimilarity(distance); // TODO - This is distance not similarity!!!

                results.add(result);
                // and set our new distance border ...
                maxDistance = results.get(results.size() - 1).getSimilarity();
            }
        }
        results.normalizeScores();
        results.invertScores(); // This is a distance (dissimilarity) and we need similarity
        return results;
    }

    @Override
    protected Result<O> getResultObject(Result<IEntry> indexEntryresult) {
        return new Result<O>((O) getDescriptorExtractor().getIndexedObject((Descriptor) indexEntryresult.getResult()));
    }
}
