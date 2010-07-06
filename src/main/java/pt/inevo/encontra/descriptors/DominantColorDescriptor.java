package pt.inevo.encontra.descriptors;

import pt.inevo.encontra.index.AbstractObject;

/**
 *
 * @author ricardo
 */
public class DominantColorDescriptor extends EncontraDescriptor {

    @Override
    public String getType() {
        return DominantColorDescriptor.class.getCanonicalName();
    }

    @Override
    public boolean extract(AbstractObject object) {
        //TO DO
        return false;
    }

    @Override
    public double getDistance(EncontraDescriptor descriptor) {
        //TO DO
        return 0;
    }

    @Override
    public String getStringRepresentation() {
        //TO DO
        return "";
    }

    @Override
    public double[] getDoubleRepresentation() {
        //TO DO
        return new double [1];
    }

}