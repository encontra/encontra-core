package pt.inevo.encontra.index;

import java.io.Serializable;


public class SimpleIndexEntry implements IndexEntry {
    private Serializable key;
    private Serializable value;
    
    public SimpleIndexEntry(){
    }

    @Override
    public Serializable getKey() {
        return key;
    }

    @Override
    public void setKey(Serializable key) {
        this.key=key;
    }

    @Override
    public Serializable getValue() {
        return value;
    }

    @Override
    public void setValue(Serializable o) {
        this.value=o;
    }

}