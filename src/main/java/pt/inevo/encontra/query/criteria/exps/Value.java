package pt.inevo.encontra.query.criteria.exps;

import java.io.Serializable;

/**
 * Interface for any non-operator in a query filter, including
 * constants, variables, and object fields.
 */
public interface Value extends Serializable {
    /**
     * Return the expected type for this value, or <code>Object</code> if
     * the type is unknown.
     */
    public Class getType();
}
