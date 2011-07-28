package pt.inevo.encontra.index.search;

import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.common.ResultSetDefaultImpl;
import pt.inevo.encontra.descriptors.Descriptor;
import pt.inevo.encontra.index.EntryProvider;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.Query;
import pt.inevo.encontra.query.QueryParserNode;
import pt.inevo.encontra.query.QueryProcessorDefaultImpl;
import pt.inevo.encontra.query.criteria.exps.Equal;
import pt.inevo.encontra.query.criteria.exps.NotEqual;
import pt.inevo.encontra.query.criteria.exps.Similar;
import pt.inevo.encontra.storage.IEntity;
import pt.inevo.encontra.storage.IEntry;


/**
 * Simple searcher
 */
public class SimpleSearcher<O extends IEntity> extends AbstractSearcher<O> {

    public SimpleSearcher() {
        queryProcessor = new QueryProcessorDefaultImpl();
    }

    @Override
    public ResultSet<O> search(Query query) {
        ResultSet<IEntry> results = new ResultSetDefaultImpl<IEntry>();

        if (query instanceof CriteriaQuery) {
            //parse the query
            QueryParserNode node = queryProcessor.getQueryParser().parse(query);
            //make the query
            if (node.predicateType.equals(Similar.class)) {
                Descriptor d = getDescriptorExtractor().extract(new IndexedObject(null, node.fieldObject));
                results = performKnnQuery(d, index.getEntryProvider().size());
            } else if (node.predicateType.equals(Equal.class)) {
                Descriptor d = getDescriptorExtractor().extract(new IndexedObject(null, node.fieldObject));
                results = performEqualQuery(d, true);
            } else if (node.predicateType.equals(NotEqual.class)) {
                Descriptor d = getDescriptorExtractor().extract(new IndexedObject(null, node.fieldObject));
                results = performEqualQuery(d, false);
            } else {
                results = queryProcessor.search(query);
            }
        }

        return getResultObjects(results);
    }

    @Override
    public ResultSet<O> similar(O entity, int knn) {
        ResultSet<IEntry> results = new ResultSetDefaultImpl<IEntry>();
        if (entity instanceof IndexedObject) {
            Descriptor d = getDescriptorExtractor().extract(entity);
            results = performKnnQuery(d, index.getEntryProvider().size());
        }
        return getResultObjects(results).getFirstResults(knn);
    }

    protected ResultSet<IEntry> performKnnQuery(Descriptor d, int maxHits) {
        Result rs = new Result(d);
        ResultSet results = new ResultSetDefaultImpl<Descriptor>(rs, maxHits);
        getResultProvider().setResultSet(results);

        EntryProvider<Descriptor> provider = index.getEntryProvider();

        for (; provider.hasNext(); ) {
            Descriptor o = provider.getNext();

            double distance = d.getDistance(o);
            Result<Descriptor> r = new Result<Descriptor>(o);
            r.setScore(distance);

            results.add(r);
        }

        results.normalizeScores();
        results.invertScores(); // This is a distance (dissimilarity) and we need similarity
        return results;
    }

    protected ResultSet<IEntry> performEqualQuery(Descriptor d, boolean equal) {

        ResultSet results = new ResultSetDefaultImpl<Descriptor>();
        getResultProvider().setResultSet(results);

        EntryProvider<Descriptor> provider = index.getEntryProvider();

        for (; provider.hasNext(); ) {
            Descriptor o = provider.getNext();

            double distance = d.getDistance(o);
            // calculate the overall max distance to normalize score afterwards
            if (equal && distance == 0) {
                Result<Descriptor> result = new Result<Descriptor>(o);
                result.setScore(distance);
                results.add(result);
                break;
            } else if (!equal && distance != 0) {
                Result<Descriptor> result = new Result<Descriptor>(o);
                result.setScore(distance);
                results.add(result);
            }
        }

        results.normalizeScores();
        results.invertScores(); // This is a distance (dissimilarity) and we need similarity
        return results;
    }

    @Override
    protected Result<O> getResultObject(Result<IEntry> entryResult) {
        return new Result<O>((O) getDescriptorExtractor().getIndexedObject((Descriptor) entryResult.getResultObject()));
    }
}