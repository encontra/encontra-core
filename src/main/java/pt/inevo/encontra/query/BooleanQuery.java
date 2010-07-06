package pt.inevo.encontra.query;

import java.util.ArrayList;
import java.util.List;
import pt.inevo.encontra.query.Query;

/**
 * Boolean Query - Can do Queries like "A and B" or "A or B", etc.
 * @author ricardo
 */
public class BooleanQuery extends Query {

    public enum BooleanType {
        AND, OR, XOR
    }

    protected ArrayList<Query> clauses;
    protected BooleanType type;

    public BooleanQuery(){
        clauses = new ArrayList<Query>();
    }

    public void addClause(Query query){
        clauses.add(query);
    }

    /**
     * Gets the type of this Boolean Query
     * @return the type
     */
    public BooleanType getBooleanType() {
        return type;
    }

    /**
     * Sets the type of this Boolean Query
     * @param type the type to set
     */
    public void setBooleanType(BooleanType type) {
        this.type = type;
    }

    /**
     * Gets the clauses used in this Boolean Query
     * @return the clauses
     */
    public List<Query> getClauses() {
        return clauses;
    }

    /**
     * Sets a group of clauses to the Query
     * @param clauses the clauses to set
     */
    public void setClauses(ArrayList<Query> clauses) {
        this.clauses = clauses;
    }

    @Override
    public QueryType getType() {
        return QueryType.BOOLEAN;
    }
}
