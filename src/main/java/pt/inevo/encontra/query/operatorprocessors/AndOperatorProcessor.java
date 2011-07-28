package pt.inevo.encontra.query.operatorprocessors;

import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.common.ResultSetDefaultImpl;
import pt.inevo.encontra.engine.QueryOperatorProcessor;
import pt.inevo.encontra.query.QueryParserNode;
import pt.inevo.encontra.query.QueryProcessorDefaultImpl;

import java.util.ArrayList;
import java.util.List;

public class AndOperatorProcessor<T> extends QueryOperatorProcessor<T> {

    @Override
    public ResultSet<T> process(QueryParserNode node) {
        ResultSet<T> results;

        List resultsParts = new ArrayList<ResultSetDefaultImpl<T>>();
        List<QueryParserNode> nodes = node.childrenNodes;

        for (QueryParserNode n : nodes) {

            QueryOperatorProcessor operator = (QueryOperatorProcessor)queryProcessor.getOperatorsProcessors().get(n.predicateType);
            ResultSet<T> r = operator.process(n);
            resultsParts.add(r);
        }

        results = ((QueryProcessorDefaultImpl)queryProcessor).getCombiner().intersect(resultsParts, node.limit, node.criteria);

        return results;
    }
}
