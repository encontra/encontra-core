package pt.inevo.encontra.storage;

import pt.inevo.encontra.query.criteria.StorageCriteria;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleObjectStorage<ID extends Number,O extends IEntity<ID>> extends GenericEntryStorage<ID,O> implements ObjectStorage<ID,O> {

    private static int counter=0;
    
    private Map<ID,O> map=new HashMap<ID,O>();

    public SimpleObjectStorage(Class<O> clazz) {
        super(clazz);
    }

    @Override
    public O get(ID id) {
        return map.get(id);
    }

    @Override
    public boolean validate(ID id, StorageCriteria criteria) {
        return true;
    }

    @Override
    public List<ID> getValidIds(StorageCriteria criteria){
        return new ArrayList<ID>();
    }


    @Override
    public O save(O obj) {
        if(obj.getId()==null)
            obj.setId((ID) new Long(++counter));
        map.put(obj.getId(),obj);
        return obj;
    }

    @Override
    public void save(O... objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void delete(O object) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


}
