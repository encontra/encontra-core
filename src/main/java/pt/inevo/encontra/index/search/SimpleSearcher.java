/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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

import java.io.Serializable;
import java.util.Random;


/**
 *
 */
public class SimpleSearcher<O extends IndexedObject> extends AbstractSearcher<O>{

    DescriptorExtractor extractor;

    public void setDescriptorExtractor(DescriptorExtractor extractor) {
        this.extractor=extractor;
    }

    public DescriptorExtractor getDescriptorExtractor() {
        return extractor;
    }

    @Override
    public boolean insert(O entry) {
        assert (entry != null);
        Descriptor descriptor=extractor.extract(entry);
        return index.insert(descriptor);
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
        ResultSet<IEntry> results = null;
        if (supportsQueryType(query.getType())) {
            if (query.getType().equals(Query.QueryType.KNN)){
                KnnQuery q = (KnnQuery) query;
                results = performKnnQuery(getDescriptorExtractor().extract((IndexedObject)q.getQuery()),q.getKnn());
            } else if (query.getType().equals(Query.QueryType.RANDOM)){
                results = performRandomQuery(query);
            }
        }

        return getResultObjects(results);
    }

    private ResultSet<IEntry> performRandomQuery(Query query){

        Random r = new Random();
        int numDocs = index.size();

        Descriptor d = index.get(r.nextInt(numDocs));
        return performKnnQuery(d,r.nextInt(10));

    }


    private ResultSet<IEntry> performKnnQuery(Descriptor d, int maxHits){
        double overallMaxDistance = 0.0;
        double maxDistance = Double.NEGATIVE_INFINITY;

        ResultSet results = new ResultSet<Descriptor>();

        int docs = index.size();
        for (int i = 0; i < docs; i++) {
            Descriptor o=index.get(i);

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
                Result<Descriptor> result=new Result<Descriptor>(o);
                result.setSimilarity(distance); // TODO - This is distance not similarity!!!
                results.add(result);
                if (distance > maxDistance) maxDistance = distance;
            } else if (distance < maxDistance) {
                // if it is nearer to the sample than at least on of the current set:
                // remove the last one ...
                results.remove(results.size()-1);
                // add the new one ...
                Result<Descriptor> result=new Result<Descriptor>(o);
                result.setSimilarity(distance); // TODO - This is distance not similarity!!!

                results.add(result);
                // and set our new distance border ...
                maxDistance = results.get(results.size()-1).getSimilarity();
            }
        }
        results.normalizeScores();
        results.invertScores(); // This is a distance and we need similarity
        return results;
    }





    /*
    public SimpleSearchHits search(Document doc, IndexReader reader) throws IOException {
        ScalableColorImpl sc = null;
        ColorLayoutImpl cl = null;
        EdgeHistogramImplementation eh = null;

        String[] cls = doc.getValues("COLORLAYOUT");
        if (cls != null && cls.length > 0)
            cl = new ColorLayoutImpl(cls[0]);
        String[] scs = doc.getValues("SCALABLECOLOR");
        if (scs != null && scs.length > 0)
            sc = new ScalableColorImpl(scs[0]);
        String[] ehs = doc.getValues("EDGEHISTOGRAM");
        if (ehs != null && ehs.length > 0)
            eh = new EdgeHistogramImplementation(ehs[0]);

        float maxDistance = findSimilar(reader, cl, sc, eh);

        return new SimpleSearchHits(this.docs, maxDistance);
    }*/

//    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
//        // get the first document:
//        if (!IndexReader.indexExists(reader.directory()))
//            throw new FileNotFoundException("No index found at this specific location.");
//        Document doc = reader.document(0);
//        ScalableColorImpl sc = null;
//        ColorLayoutImpl cl = null;
//        EdgeHistogramImplementation eh = null;
//
//        String[] cls = doc.getValues(DocumentBuilder.FIELD_NAME_COLORLAYOUT);
//        if (cls != null && cls.length > 0)
//            cl = new ColorLayoutImpl(cls[0]);
//        String[] scs = doc.getValues(DocumentBuilder.FIELD_NAME_SCALABLECOLOR);
//        if (scs != null && scs.length > 0)
//            sc = new ScalableColorImpl(scs[0]);
//        String[] ehs = doc.getValues(DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM);
//        if (ehs != null && ehs.length > 0)
//            eh = new EdgeHistogramImplementation(ehs[0]);
//
//        HashMap<Float, List<String>> duplicates = new HashMap<Float, List<String>>();
//
//        // find duplicates ...
//        boolean hasDeletions = reader.hasDeletions();
//
//        int docs = reader.numDocs();
//        int numDuplicates = 0;
//        for (int i = 0; i < docs; i++) {
//            if (hasDeletions && reader.isDeleted(i)) {
//                continue;
//            }
//            Document d = reader.document(i);
//            float distance = getDistance(d, cl, sc, eh);
//
//            if (!duplicates.containsKey(distance)) {
//                duplicates.put(distance, new LinkedList<String>());
//            } else {
//                numDuplicates++;
//            }
//            duplicates.get(distance).add(d.getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
//        }
//
//        if (numDuplicates == 0) return null;
//
//        LinkedList<List<String>> results = new LinkedList<List<String>>();
//        for (float f : duplicates.keySet()) {
//            if (duplicates.get(f).size() > 1) {
//                results.add(duplicates.get(f));
//            }
//        }
//        return new SimpleImageDuplicates(results);
//    }

    @Override
    protected Result<O> getResultObject(Result<IEntry> indexEntryresult){
        return new Result<O>((O)getDescriptorExtractor().getIndexedObject((Descriptor)indexEntryresult.getResult()));
    }

}
