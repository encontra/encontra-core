package pt.inevo.encontra.engine;

import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.IndexedObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class SimpleIndexedObjectFactory extends AnnotatedIndexedObjectFactory {

    @Override
    protected List<IndexedObject> createObjects(List<IndexedObjectFactory.IndexedField> indexedFields) {
        List<IndexedObject> result = new ArrayList<IndexedObject>();
        for (IndexedObjectFactory.IndexedField field : indexedFields) {
            assert(field.id != null);
            result.add(new IndexedObject(field.id, field.name, field.object, field.boost));
        }
        return result;
    }
}