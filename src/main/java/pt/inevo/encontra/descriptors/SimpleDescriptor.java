package pt.inevo.encontra.descriptors;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;


public class SimpleDescriptor implements Descriptor{

    private Serializable id;
    private String value;

    @Override
    public String getName() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(Object o) {
        this.value=(String)o;
    }

    public Serializable getId() {
        return id;
    }

    public void setId(Serializable id) {
        this.id = id;
    }

    @Override
    public double getDistance(Descriptor other) {
       throw new NotImplementedException(); // TODO - This is an ugly hack
    }
}
