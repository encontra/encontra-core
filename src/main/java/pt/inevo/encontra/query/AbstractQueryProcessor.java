package pt.inevo.encontra.query;

import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.common.ResultSetDefaultImpl;
import pt.inevo.encontra.engine.IQueryProcessor;
import pt.inevo.encontra.query.criteria.exps.*;
import pt.inevo.encontra.storage.IEntity;

public abstract class AbstractQueryProcessor<E extends IEntity> extends IQueryProcessor<E> {

    public AbstractQueryProcessor(){
        super();
    }

    public AbstractQueryProcessor(Class clazz) {
        super(clazz);
    }

    @Override
    public ResultSet<E> process(QueryParserNode node) {
        if (node.predicateType.equals(And.class)) {
            return processAND(node);
        } else if (node.predicateType.equals(Or.class)) {
            return processOR(node);
        } else if (node.predicateType.equals(Similar.class)
                || node.predicateType.equals(Equal.class)
                || node.predicateType.equals(NotEqual.class)) {
            return processSIMILAR(node, true);
        } else {
            return new ResultSetDefaultImpl<E>();
        }
    }
}
