package pt.inevo.encontra.engine;

import pt.inevo.encontra.common.DefaultResultProvider;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.common.ResultSetDefaultImpl;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.search.AbstractSearcher;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.QueryProcessorDefaultImpl;
import pt.inevo.encontra.query.criteria.CriteriaBuilderImpl;
import pt.inevo.encontra.storage.IEntity;
import pt.inevo.encontra.storage.IEntry;
import pt.inevo.encontra.storage.SimpleObjectStorage;

/**
 * A generic and simple engine / searcher.
 * @author Ricardo
 */
public class SimpleEngine<O extends IEntity> extends AbstractSearcher<O> {

    public SimpleEngine() {
        //Creating the storage
        storage = new SimpleObjectStorage(IEntity.class);
        setQueryProcessor(new QueryProcessorDefaultImpl());
        setIndexedObjectFactory(new SimpleIndexedObjectFactory());
        setResultProvider(new DefaultResultProvider());
    }

    @Override
    public ResultSet<O> similar(O object, int knn) {
        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();

        CriteriaQuery query = cb.createQuery();
        if (object instanceof IndexedObject) {
            IndexedObject o = (IndexedObject)object;
            query.where(cb.similar(o.getName(), o.getValue()));
            return search(query);
        } else {
            // TODO - must break down the IEntity? Should this be done here!
        }
        return new ResultSetDefaultImpl<O>();
    }

    @Override
    protected Result<O> getResultObject(Result<IEntry> entryResult) {
        return new Result<O>((O) storage.get(entryResult.getResultObject().getId()));
    }
}