package pt.inevo.encontra.index;

import pt.inevo.encontra.storage.IEntry;

public class SimpleIndexEntry<O extends IEntry> implements IndexEntry {
    private Object key;
    private Object value;
    
    public SimpleIndexEntry(){
    }

    @Override
    public Object getKey() {
        return key;
    }

    @Override
    public void setKey(Object key) {
        this.key=key;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object o) {
       this.value=o;
    }

}
