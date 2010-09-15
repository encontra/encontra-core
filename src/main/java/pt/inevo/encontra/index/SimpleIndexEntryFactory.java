package pt.inevo.encontra.index;

import pt.inevo.encontra.storage.IEntry;

public class SimpleIndexEntryFactory<O extends IEntry> extends IndexEntryFactory<O,IndexEntry>{

    public SimpleIndexEntryFactory(Class objectClass){
        super(SimpleIndexEntry.class, objectClass);
    }

    @Override
    protected IndexEntry setupIndexEntry(O object, IndexEntry entry) {
        entry.setKey(object.getId());
        entry.setValue(object.getValue());
        return entry;
    }

    @Override
    protected O setupObject(IndexEntry entry, O object) {
        object.setId(entry.getKey());
        object.setValue(entry.getValue());
        return object;
    }

}