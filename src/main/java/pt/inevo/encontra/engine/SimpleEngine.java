package pt.inevo.encontra.engine;

import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.index.search.AbstractSearcher;
import pt.inevo.encontra.storage.IEntity;
import pt.inevo.encontra.storage.IEntry;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;

/**
 * A generic and simple engine / searcher.
 * @author Ricardo
 */
public class SimpleEngine<O extends IEntity> extends AbstractSearcher<O> {

    @Override
    protected Result<O> getResultObject(Result<IEntry> entryresult, String criteria) {
        return new Result<O>((O) storage.get(entryresult.getResultObject().getId(), criteria));
    }
}