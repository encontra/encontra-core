package pt.inevo.encontra.index.search;

import pt.inevo.encontra.descriptors.Descriptor;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.Result;
import pt.inevo.encontra.index.ResultSet;
import pt.inevo.encontra.query.QueryParserNode;
import pt.inevo.encontra.query.Query;
import pt.inevo.encontra.query.QueryProcessorDefaultImpl;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.criteria.exps.Similar;
import pt.inevo.encontra.storage.IEntity;
import pt.inevo.encontra.storage.IEntry;

/**
 * Simple searcher
 */
public class SimpleSearcher<O extends IEntity> extends AbstractSearcher<O> {

    protected DescriptorExtractor extractor;

    public SimpleSearcher() {
        queryProcessor = new QueryProcessorDefaultImpl();
    }

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
    public boolean remove(O entry) {
        assert (entry != null);
        Descriptor descriptor = extractor.extract(entry);
        return index.remove(descriptor);
    }

    @Override
    public ResultSet<O> search(Query query) {
        ResultSet<IEntry> results = new ResultSet<IEntry>();

        if (query instanceof CriteriaQuery) {

            CriteriaQuery q = (CriteriaQuery) query;
            if (q.getRestriction().getClass().equals(Similar.class)) {
                QueryParserNode nodes = queryProcessor.getQueryParser().parse(query);
                //can only process simple queries: similar, equals, etc.
                if (nodes.predicateType.equals(Similar.class)) {
                    Descriptor d = getDescriptorExtractor().extract(new IndexedObject(null,nodes.fieldObject));
                    results = performKnnQuery(d, 10);
                }
            } else {
                return getResultObjects(queryProcessor.search(query));
            }
        }

        return getResultObjects(results);
    }

    protected ResultSet<IEntry> performKnnQuery(Descriptor d, int maxHits) {
        double overallMaxDistance = 0.0;
        double maxDistance = Double.NEGATIVE_INFINITY;

        ResultSet results = new ResultSet<Descriptor>();

        for (; index.hasNext();) {
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
        //resets the entry provider for future calls
        index.begin();

        results.normalizeScores();
        results.invertScores(); // This is a distance (dissimilarity) and we need similarity
        return results;
    }

    @Override
    protected Result<O> getResultObject(Result<IEntry> indexEntryresult) {
        return new Result<O>((O) getDescriptorExtractor().getIndexedObject((Descriptor) indexEntryresult.getResult()));
    }
}
