package pt.inevo.encontra.query.criteria.exps;

import pt.inevo.encontra.query.criteria.Expression;

import java.io.Serializable;

/**
 *  Struct to hold the state of a parsed expression query.
 *
 * from org.apache.openjpa.kernel.exps.QueryExpressions
 */
public class QueryExpressions  implements Serializable {
    public static final int DISTINCT_AUTO = 2 << 0;
    public static final int DISTINCT_TRUE = 2 << 1;
    public static final int DISTINCT_FALSE = 2 << 2;
    public static final Value[] EMPTY_VALUES = new Value[0];
    public static final String[] EMPTY_STRINGS = new String[0];
    public static final boolean[] EMPTY_BOOLEANS = new boolean[0];

    public int distinct = DISTINCT_AUTO;

    public Class<?> resultClass = null;
    public Expression filter = null;

    public boolean[] ascending = EMPTY_BOOLEANS;
    public Value[] ordering = EMPTY_VALUES;
    public String[] orderingClauses = EMPTY_STRINGS;
    public String[] orderingAliases = EMPTY_STRINGS;

    public boolean isDistinct() {
        return distinct != DISTINCT_FALSE;
    }
}
