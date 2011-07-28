package pt.inevo.encontra.test.entities;

import pt.inevo.encontra.index.IndexedObject;

public class StringObject extends IndexedObject<Long, String> {

    public StringObject() {
        super();
    }

    public StringObject(String obj) {
        super();
        setValue(obj);
    }
}
