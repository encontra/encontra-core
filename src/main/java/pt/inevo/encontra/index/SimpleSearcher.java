/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.inevo.encontra.index;


import pt.inevo.encontra.descriptors.EncontraDescriptor;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SimpleSearcher<O extends AbstractObject,D extends EncontraDescriptor<O>> {
    private int maxHits = 10;

    private ResultSet results; // Todo use a set to avoid duplicates!

    Class<O> objectClass;
    D _descriptor;
    
    public SimpleSearcher(Class<O> clazz,D descriptor,int maxHits) {
        this.maxHits = maxHits;
        this.objectClass=clazz;
        _descriptor=descriptor;
    }

    protected O newAbstractObject(){
        O o=null;
        try {
            o=objectClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return o;
    }


    public ResultSet search(Object object, Index reader) throws IOException {
        O query=newAbstractObject();
        query.setObject(object);
        _descriptor.extract(query);

        float maxDistance = findSimilar(reader, _descriptor);
        results = new ResultSet();
        results
        return new ResultSet(this.results, maxDistance);
    }

    /**
     * @param reader
     * @param descriptor
     * @return the maximum distance found for normalizing.
     * @throws java.io.IOException
     */
    private float findSimilar(Index reader, D  descriptor) throws IOException {
        double maxDistance = -1f, overallMaxDistance = -1f;

        // clear result set ...
        results.clear();

        int docs = reader.size();
        for (int i = 0; i < docs; i++) {

            D objDescriptor = (D) reader.get(i);
            O object=newAbstractObject();
            object.setId(objDescriptor.getId());
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

                results.add(new Result(e.getValue()));
                if (distance > maxDistance) maxDistance = distance;
            } else if (distance < maxDistance) {
                // if it is nearer to the sample than at least on of the current set:
                // remove the last one ...
                results.remove(results.last());
                // add the new one ...
                results.add(new Result(distance, d));
                // and set our new distance border ...
                maxDistance = results.last().getDistance();
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

}
