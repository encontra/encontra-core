package pt.inevo.encontra.storage;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SimpleObjectStorage<ID extends Serializable,V extends Serializable,O extends StorableObject<ID, ?, V>> extends GenericEntryStorage<ID,O> implements ObjectStorage<ID,O> {
    private Map<ID,V> map=new HashMap<ID,V>();

    public SimpleObjectStorage(Class<O> clazz) {
        super(clazz);
    }

    @Override
    public O get(ID id) {
        O object=newEntryValue();
        object.setId(id);
        object.setValue(map.get(id));
        return object;
    }

    @Override
    public List<O> get(ID... ids) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<O> getAll() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void save(O obj) {
        map.put(obj.getId(),obj.getValue());
    }

    @Override
    public void save(O... objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void delete(ID id) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


}
