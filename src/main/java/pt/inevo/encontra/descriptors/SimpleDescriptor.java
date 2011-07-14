package pt.inevo.encontra.descriptors;

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

    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public void setId(Serializable id) {
        this.id = id;
    }

    @Override
    public double getDistance(Descriptor other) {
       return 0;
    }

    @Override
    public double getNorm() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
