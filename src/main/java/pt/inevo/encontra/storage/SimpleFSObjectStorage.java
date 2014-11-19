package pt.inevo.encontra.storage;

import pt.inevo.encontra.query.criteria.StorageCriteria;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by jpvguerreiro on 10/27/2014.
 */
public class SimpleFSObjectStorage <ID extends Number,O extends IEntity<ID>> extends GenericEntryStorage<ID,O> implements ObjectStorage<ID,O> {
    String rootPath;
    public SimpleFSObjectStorage(Class<O> clazz, String path) {super(clazz); this.rootPath = path;}

    @Override
    public O get(ID id) {
        O obj = null;
        try {
            FileInputStream fin = new FileInputStream(this.rootPath+id.toString());
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
        ID objId = obj.getId();

        //The Id should be null and should be assigned here
        if (objId == null) {
            objId = (ID) new Long(uniqueCurrentTimeMS());
            obj.setId(objId);
        }

        //The objects should be stored all at the same time to avoid duplications
        String objPath = this.rootPath+objId.toString();
        File currentFile = new File(objPath);
        if (!currentFile.exists()) {
            try {
                currentFile.getParentFile().mkdirs();
                FileOutputStream fout = new FileOutputStream(objPath);
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(obj);
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

    private static final AtomicLong LAST_TIME_MS = new AtomicLong();
    public static long uniqueCurrentTimeMS() {
        long now = System.currentTimeMillis();
        while(true) {
            long lastTime = LAST_TIME_MS.get();
            if (lastTime >= now)
                now = lastTime+1;
            if (LAST_TIME_MS.compareAndSet(lastTime, now))
                return now;
        }
    }
}
