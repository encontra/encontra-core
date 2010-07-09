/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.inevo.encontra.index.search;


import pt.inevo.encontra.common.distance.HasDistance;
import pt.inevo.encontra.descriptors.Descriptor;
import pt.inevo.encontra.index.Index;
import pt.inevo.encontra.index.IndexEntry;
import pt.inevo.encontra.index.Result;
import pt.inevo.encontra.index.ResultSet;
import pt.inevo.encontra.query.Query;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SimpleSearcher<E extends IndexEntry> extends MultiIndexSearcher<E>{
    private int maxHits = 10;

    private ResultSet<E> results; // Todo use a set to avoid duplicates!

    public SimpleSearcher(int maxHits) {
        this.maxHits = maxHits;
    }



    public ResultSet search(HasDistance query, Index reader) throws IOException {
        double maxDistance = findSimilar(reader, query);
        results.normalizeScores(maxDistance);
        results.invertScores(); // This is a distance and we need similarity
        return results;
    }

    /**
     * @param reader
     * @param descriptor
     * @return the maximum distance found for normalizing.
     * @throws java.io.IOException
     */
    private double findSimilar(Index<E> reader, HasDistance  descriptor) throws IOException {
        double maxDistance = -1f, overallMaxDistance = -1f;

        // clear result set ...
        results.clear();

        int docs = reader.size();
        for (int i = 0; i < docs; i++) {
            E entry=reader.get(i);
            HasDistance objDescriptor = (HasDistance) entry.getValue();
            double distance = objDescriptor.getDistance(descriptor);
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
                Result<E> result=new Result<E>(entry);
                result.setSimilarity(distance); // TODO - This is distance not similarity!!!
                results.add(result);
                if (distance > maxDistance) maxDistance = distance;
            } else if (distance < maxDistance) {
                // if it is nearer to the sample than at least on of the current set:
                // remove the last one ...
                results.remove(results.size()-1);
                // add the new one ...
                Result<E> result=new Result<E>(entry);
                result.setSimilarity(distance); // TODO - This is distance not similarity!!!

                results.add(result);
                // and set our new distance border ...
                maxDistance = results.get(results.size()-1).getSimilarity();
            }
        }
        return maxDistance;
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
    public ResultSet<E> search(Query query) {
        ResultSet results = new ArrayList<ResultSet>();
        //sends the query to all the indexes that support that query type
        for (Index idx : indexes) {

        }
    }


    @Override
    public ResultSet<E> search(Index idx, Query query) {
        if (idx.supportsQueryType(query.getType())) { //if supports type then make the query
            return idx.search(query);
        }
        return null;
    }

    @Override
    public ResultSet<E> search(Index idx, Query[] queries) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
