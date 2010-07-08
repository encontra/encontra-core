package pt.inevo.encontra.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Boolean Query - Can do Queries like "A and B" or "A or B", etc.
 * @author ricardo
 */
public class BooleanQuery extends Query {

    public enum BooleanType {
        AND, OR, XOR
    }

    protected ArrayList<Query> clauses;
    protected BooleanType booleanType;

    public BooleanQuery(){
        clauses = new ArrayList<Query>();
        super.type = QueryType.BOOLEAN;
    }

    public void addClause(Query query){
        clauses.add(query);
    }

    /**
     * Gets the type of this Boolean Query
     * @return the type
     */
    public BooleanType getBooleanType() {
        return booleanType;
    }

    /**
     * Sets the type of this Boolean Query
     * @param type the type to set
     */
    public void setBooleanType(BooleanType type) {
        this.booleanType = type;
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
}
