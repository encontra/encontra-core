package pt.inevo.encontra.query;

import pt.inevo.encontra.engine.IQueryProcessor;
import pt.inevo.encontra.engine.QueryOperatorProcessor;
import pt.inevo.encontra.index.search.AbstractSearcher;
import pt.inevo.encontra.query.criteria.exps.*;
import pt.inevo.encontra.query.operatorprocessors.AndOperatorProcessor;
import pt.inevo.encontra.query.operatorprocessors.OrOperatorProcessor;
import pt.inevo.encontra.query.operatorprocessors.SimilarOperatorProcessor;
import pt.inevo.encontra.storage.IEntity;

import java.util.logging.Logger;

/**
 * Default implementation for the query processor.
 * Cascade Query Processor.
 *
 * @author Ricardo
 * @version 1.0
 */
public class QueryProcessorDefaultImpl<E extends IEntity> extends IQueryProcessor<E> {

    protected ResultSetOperations combiner;

    public QueryProcessorDefaultImpl() {
        this(null);

        //initializing the default query operators
        QueryOperatorProcessor operator = new SimilarOperatorProcessor();
        operator.setQueryProcessor(this);
        getOperatorsProcessors().put(Similar.class, operator);
        getOperatorsProcessors().put(Equal.class, operator);
        getOperatorsProcessors().put(NotEqual.class, operator);

        operator = new AndOperatorProcessor();
        operator.setQueryProcessor(this);
        getOperatorsProcessors().put(And.class, operator);

        operator = new OrOperatorProcessor();
        operator.setQueryProcessor(this);
        getOperatorsProcessors().put(Or.class, operator);
    }

    public QueryProcessorDefaultImpl(Class clazz) {
        super(clazz);
        setCombiner(new ResultSetOperations());
        queryParser = new QueryParserDefaultImpl();
        logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void setTopSearcher(AbstractSearcher topSearcher) {
        super.setTopSearcher(topSearcher);
        //the combiner must know the top level object storage
        getCombiner().setStorage(topSearcher.getObjectStorage());
    }

    public ResultSetOperations getCombiner() {
        return combiner;
    }

    public void setCombiner(ResultSetOperations combiner) {
        this.combiner = combiner;
    }
}
