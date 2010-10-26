package pt.inevo.encontra;

import pt.inevo.encontra.index.IndexedObject;

/**
 * String object just for testing.
 * @author ricardo
 */
public class StringObject extends IndexedObject<Long,String> {

    private static long counter=0;
    protected String str;

    public StringObject(){
       this("");
    }
    
    public StringObject(String s) {
        super(counter++, s);
        this.str = s;
    }


    @Override
    public String toString() {
        return str;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StringObject) {
            StringObject o = (StringObject) obj;
            return str.equals(o.getValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.str != null ? this.str.hashCode() : 0);
        return hash;
    }

}
