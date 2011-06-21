package pt.inevo.encontra.query;

import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.common.ResultSetDefaultImpl;
import pt.inevo.encontra.engine.IQueryProcessor;
import pt.inevo.encontra.query.criteria.exps.*;
import pt.inevo.encontra.storage.IEntity;

public abstract class AbstractQueryProcessor<E extends IEntity> extends IQueryProcessor<E> {

    @Override
    public ResultSet<E> process(QueryParserNode node) {
        if (node.predicate instanceof And) {
            return processAND(node);
        } else if (node.predicate instanceof Or) {
            return processOR(node);
        } else if (node.predicate instanceof Similar
                || node.predicate instanceof Equal
                || node.predicate instanceof NotEqual) {
            return processSIMILAR(node);
        } else {
            return new ResultSetDefaultImpl<E>();
        }
    }
}
