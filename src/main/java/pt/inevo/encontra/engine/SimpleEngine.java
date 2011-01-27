package pt.inevo.encontra.engine;


import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.index.search.AbstractSearcher;
import pt.inevo.encontra.index.search.ResultsProvider;
import pt.inevo.encontra.storage.IEntity;
import pt.inevo.encontra.storage.IEntry;

/**
 * A generic and simple engine / searcher.
 * @author Ricardo
 */
public class SimpleEngine<O extends IEntity> extends AbstractSearcher<O> {

    @Override
    protected Result<O> getResultObject(Result<IEntry> entryresult) {
        return new Result<O>((O) storage.get(entryresult.getResultObject().getId()));
    }
}