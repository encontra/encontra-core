package pt.inevo.encontra.storage;

import pt.inevo.encontra.query.criteria.StorageCriteria;

import java.io.*;
import java.util.*;

/**
 * Created by jpvguerreiro on 10/27/2014.
 */
public class SimpleFSObjectStorage <ID extends Number,O extends IEntity<ID>> extends GenericEntryStorage<ID,O> implements ObjectStorage<ID,O> {

    private static int counter = 0;

    private Map<ID, O> map = new HashMap<ID, O>();

    public SimpleFSObjectStorage(Class<O> clazz) {super(clazz);}

    @Override
    public O get(ID id) {
        O obj = null;
        try {
            FileInputStream fin = new FileInputStream("data/objects/"+id.toString());
            ObjectInputStream ois = new ObjectInputStream(fin);
            obj = (O) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public boolean validate(ID id, StorageCriteria criteria) {
        return true;
    }

    @Override
    public List<ID> getValidIds(StorageCriteria criteria) {
        return new ArrayList<ID>();
    }


    @Override
    public O save(O obj) {
        String obj_string = obj.toString();
        ID objId = obj.getId();

        //Most of the times, the ImageModel was already loaded, so the ID is not null.
        if (objId == null) {
            objId = (ID) new Long(++counter);
            obj.setId(objId);
        }

        //The objects should be stored all at the same time to avoid problems
        //TODO find an incremental error-free way to deal with the storage
        String objPath = "data/objects/"+objId.toString();
        if (!new File(objPath).exists()) {
            try {
                FileOutputStream fout = new FileOutputStream(objPath);
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(obj_string);
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
