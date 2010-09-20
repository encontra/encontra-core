package pt.inevo.encontra.query;

import pt.inevo.encontra.query.criteria.exps.InMemoryExpressionFactory;
import pt.inevo.encontra.query.criteria.exps.QueryExpressions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Runs the expression query in memory.
 */
public class InMemoryExecutor
        extends AbstractExpressionExecutor
        implements Executor, Serializable {

//    private final ClassMetaData _meta;
//    private final boolean _subs;
//    private final InMemoryExpressionFactory _factory;
//    private final QueryExpressions[] _exps;
//    private final Class[] _projTypes;
//
//    public InMemoryExecutor(Query q,
//                            ClassMetaData candidate, boolean subclasses,
//                            ExpressionParser parser, Object parsed) {
//        _meta = candidate;
//        _subs = subclasses;
//        _factory = new InMemoryExpressionFactory();
//
//        _exps = new QueryExpressions[] {
//                parser.eval(parsed, q, _factory, _meta)
//        };
//    }
//
//    public QueryExpressions[] getQueryExpressions() {
//        return _exps;
//    }
//
//    public ResultObjectProvider executeQuery(Query q,Object[] params, Range range) {
//        // execute in memory for candidate collection;
//        // also execute in memory for transactional extents
//        Collection coll = q.getContext().getCandidateCollection();
//        Iterator itr;
//
//        itr = coll.iterator();
//
//        // find matching objects
//        List results = new ArrayList();
//
//        try {
//            Object obj;
//            while (itr.hasNext()) {
//                obj = itr.next();
//                if (_factory.matches(_exps[0], _meta, _subs, obj,params))
//                    results.add(obj);
//            }
//        }
//        finally {
//            ImplHelper.close(itr);
//        }
//
//
//        // apply projections, order results, and filter duplicates
//        results = _factory.order(_exps[0], results, params);
//        results = _factory.distinct(_exps[0], coll == null, results);
//
//        ResultObjectProvider rop = new ListResultObjectProvider(results);
//        if (range.start != 0 || range.end != Long.MAX_VALUE)
//            rop = new RangeResultObjectProvider(rop, range.start,range.end);
//        return rop;
//    }
//
//    public String[] getDataStoreActions(StoreQuery q, Object[] params,
//                                        Range range) {
//        // in memory queries have no datastore actions to perform
//        return StoreQuery.EMPTY_STRINGS;
//    }
//
//    public Object getOrderingValue(StoreQuery q, Object[] params,
//                                   Object resultObject, int orderIndex) {
//        // if this is a projection, then we have to order on something
//        // we selected
//        if (_exps[0].projections.length > 0) {
//            String ordering = _exps[0].orderingClauses[orderIndex];
//            for (int i = 0; i < _exps[0].projectionClauses.length; i++)
//                if (ordering.equals(_exps[0].projectionClauses[i]))
//                    return ((Object[]) resultObject)[i];
//
//            throw new InvalidStateException(_loc.get
//                    ("merged-order-with-result", q.getContext().getLanguage(),
//                            q.getContext().getQueryString(), ordering));
//        }
//
//        // use the parsed ordering expression to extract the ordering value
//        Val val = (Val) _exps[0].ordering[orderIndex];
//        return val.evaluate(resultObject, resultObject, q.getContext().
//                getStoreContext(), params);
//    }
//
//    public Class[] getProjectionTypes(StoreQuery q) {
//        return _projTypes;
//    }
//
//    /**
//     * Throws an exception if a variable is found.
//     */
//    private static class AssertNoVariablesExpressionVisitor
//            extends AbstractExpressionVisitor {
//
//        private final QueryContext _ctx;
//
//        public AssertNoVariablesExpressionVisitor(QueryContext ctx) {
//            _ctx = ctx;
//        }
//
//        public void enter(Value val) {
//            if (!val.isVariable())
//                return;
//            throw new UnsupportedException(_loc.get("inmem-agg-proj-var",
//                    _ctx.getCandidateType(), _ctx.getQueryString()));
//        }
//    }
}
