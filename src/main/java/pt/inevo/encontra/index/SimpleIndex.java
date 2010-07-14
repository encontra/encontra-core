package pt.inevo.encontra.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pt.inevo.encontra.descriptors.Key;
import pt.inevo.encontra.query.Query;
import pt.inevo.encontra.query.Query.QueryType;
import pt.inevo.encontra.query.RangeQuery;
import pt.inevo.encontra.storage.IEntity;
import pt.inevo.encontra.storage.IEntry;

/**
 * A linear implementation of an Index.
 * @author ricardo
 */
public class SimpleIndex<E extends IEntry> extends AbstractIndex<E> {

    protected ArrayList<IndexEntry> idx;
    protected static QueryType [] supportedTypes  =
            new QueryType[]{QueryType.RANDOM, QueryType.RANGE,
                                QueryType.TEXT, QueryType.KNN,
                                QueryType.BOOLEAN};


    public SimpleIndex(Class objectClass) {
        idx = new ArrayList<IndexEntry>();
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
    public E get(int i) {
        return (E) getEntryFactory().getObject(idx.get(i));
    }

    @Override
    public boolean contains(E object){
        if (idx.contains(object)){
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
