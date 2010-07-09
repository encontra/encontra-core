package pt.inevo.encontra.index;

import pt.inevo.encontra.storage.StorableObject;


public class SimpleIndexEntryFactory<O extends StorableObject> implements IndexEntryFactory<O,IndexEntry>{

    @Override
    public IndexEntry createIndexEntry(O object) {
        SimpleIndexEntry<O> entry = new SimpleIndexEntry<O>();
        entry.setKey(object.getId());
        entry.setValue(object.getValue());
        return entry;
    }

    @Override
    public Object getObjectId(IndexEntry entry) {
        return entry.getKey();
    }
}
