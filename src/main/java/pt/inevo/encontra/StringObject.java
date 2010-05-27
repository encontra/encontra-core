package pt.inevo.encontra;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import pt.inevo.encontra.index.AbstractObject;

/**
 * String object just for testing
 * @author ricardo
 */
public class StringObject extends AbstractObject {

    protected String str;

    public StringObject(String s) {
        this.str = s;
    }

    /**
     * @return the str
     */
    public String getStr() {
        return str;
    }

    /**
     * @param str the str to set
     */
    public void setStr(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StringObject) {
            StringObject o = (StringObject) obj;
            return str.equals(o.getStr());
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
