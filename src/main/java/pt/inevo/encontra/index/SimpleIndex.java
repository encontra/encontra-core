package pt.inevo.encontra.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import pt.inevo.encontra.storage.IEntity;
import pt.inevo.encontra.storage.IEntry;

/**
 * A linear implementation of an Index.
 * @author ricardo
 */
public class SimpleIndex<E extends IEntry> extends AbstractIndex<E> {

    protected ArrayList<IndexEntry> idx;
    protected int iterator;

    public SimpleIndex(Class objectClass) {
        idx = new ArrayList<IndexEntry>();
        iterator = 0;
        this.setEntryFactory(new SimpleIndexEntryFactory(objectClass));
    }


    @Override
    public boolean insert(E entry) {
        return idx.add(getEntryFactory().createIndexEntry(entry));
    }

    @Override
    public boolean remove(E entry) {
        return idx.remove(getEntryFactory().createIndexEntry(entry));
    }

    @Override
    public int size() {
        return idx.size();
    }

    @Override
    public boolean contains(E object){
        if (idx.contains(getEntryFactory().createIndexEntry(object))){
            return true;
        }
        return false;
    }

    @Override
    public List<E> getAll() {
        List<E> list=new ArrayList<E>();
        for(IndexEntry entry : idx) {
             list.add((E)getEntryFactory().getObject(entry));
        }
        return list;
    }

    @Override
    public E getFirst() {
        if (idx.size() > 0){
            return (E)getEntryFactory().getObject(idx.get(0));
        }
        return null;
    }

    @Override
    public E getLast() {
        if (idx.size() > 0){
            return (E)getEntryFactory().getObject(idx.get(idx.size()));
        }
        return null;
    }

    @Override
    public void begin() {
        iterator = 0;
    }

    @Override
    public void end() {
        iterator = idx.size();
    }

    @Override
    public boolean setCursor(E entry) {
        IndexEntry iEntry = getEntryFactory().createIndexEntry(entry);
        for (int i = 0; i < idx.size() ; i++){
            if (idx.get(i).equals(iEntry)){
                iterator = i;
                return true;
            }
        }
        //that entry was not found in the index
        return false;
    }

    @Override
    public E getEntry(Serializable key) {
        for (IndexEntry entry: idx){
            if (entry.getKey().equals(key))
                return (E)getEntryFactory().getObject(entry);
        }
        //entry was not found, so return null
        return null;
    }

    @Override
    public E getNext() {
        if (iterator < idx.size() && idx.size() > 0){
            return (E)getEntryFactory().getObject(idx.get(iterator++));
        }
        return null;
    }

    @Override
    public E getPrevious() {
        if (iterator > 0 && idx.size() > 0){
            return (E)getEntryFactory().getObject(idx.get(iterator--));
        }
        return null;
    }
    
        @Override
    public boolean hasNext() {
        if (iterator < idx.size()) return true;
        else return false;
    }

    @Override
    public boolean hasPrevious() {
        if (iterator > 0) return true;
        else return false;
    }

    @Override
    public IEntity get(Serializable id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IEntity save(IEntity object) {
        return null;
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void save(IEntity... objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void delete(IEntity object) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}