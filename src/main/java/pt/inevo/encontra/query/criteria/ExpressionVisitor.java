package pt.inevo.encontra.query.criteria;

import java.util.HashSet;
import java.util.Set;

/**
 * A visitor for Criteria Expression nodes.
 *
 * @author Pinaki Poddar
 * @since 2.0.0
 *
 */
public interface ExpressionVisitor {
    // Enumerates order of traversal of nodes
    public static enum TraversalStyle {
        INFIX,    // operand1 operator operand2   e.g. a + b
        POSTFIX,  // operand1 operand2 operator   e.g. a b +
        PREFIX,   // operator operand1 operand2   e.g. + a b
        FUNCTION  // operator(operand1, operand2) e.g. f(a,b)
    }

    /**
     * Enter the given node.
     */
    void enter(Expression expr);

    /**
     * Exit the given node.
     */
    void exit(Expression expr);

    /**
     * Affirms if this node has been visited.
     */
    boolean isVisited(Expression expr);

    /**
     * Get the traversal style of the children of the given node.
     */
    TraversalStyle getTraversalStyle(Expression expr);

    /**
     * An abstract implementation that can detect cycles during traversal.
     *
     */
    public static abstract class AbstractVisitor implements ExpressionVisitor {
        protected final Set<Expression> _visited = new HashSet<Expression>();

        /**
         * Remembers the node being visited.
         */
        public void exit(Expression expr) {
            _visited.add(expr);
        }

        /**
         * Affirms if this node has been visited before.
         */
        public boolean isVisited(Expression expr) {
            return _visited.contains(expr);
        }

        /**
         * Returns PREFIX as the default traversal style.
         */
        public TraversalStyle getTraversalStyle(Expression expr) {
            return TraversalStyle.PREFIX;
        }
    }


}
