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
    protected Result<O> getResultObject(Result<IEntry> entryResult) {
        return new Result<O>((O) storage.get( Long.parseLong((String) entryResult.getResultObject().getId())));
    }
}