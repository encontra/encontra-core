package pt.inevo.encontra.index;

import java.io.Serializable;


public class SimpleIndexEntry implements IndexEntry {
    private Serializable key;
    private Object value;
    
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
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object o) {
       this.value=o;
    }

}
