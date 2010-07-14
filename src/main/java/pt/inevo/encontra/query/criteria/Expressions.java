package pt.inevo.encontra.query.criteria;

public class Expressions {

    /**
      * Visits the given expression and the given children recursively.
      * The order of traversal depends on the parent and is determined by the visitor.
      */
     public static void acceptVisit(ExpressionVisitor visitor, Expression parent, Expression<?>...exprs) {
         if (parent == null)
             return;
         ExpressionVisitor.TraversalStyle traversal = visitor.getTraversalStyle(parent);
         switch (traversal) {
         case INFIX :
             if (exprs == null || exprs.length == 0) {
                 visitor.enter(parent);
                 visitor.exit(parent);
                 return;
             }
             for (int i = 0; i < exprs.length; i++) {
                 ExpressionImpl<?> e = (ExpressionImpl<?>)exprs[i];
                 if (e != null) e.acceptVisit(visitor);
                 if (i + 1 != exprs.length) {
                     visitor.enter(parent);
                     visitor.exit(parent);
                 }
             }
             break;
         case POSTFIX:
             visitChildren(visitor,exprs);
             visitor.enter(parent);
             visitor.exit(parent);
             break;
         case PREFIX :
             visitor.enter(parent);
             visitor.exit(parent);
             visitChildren(visitor,exprs);
             break;
         case FUNCTION:
             visitor.enter(parent);
             visitChildren(visitor, exprs);
             visitor.exit(parent);
             break;
         }
     }

     static void visitChildren(ExpressionVisitor visitor, Expression<?>...exprs) {
         for (int i = 0; exprs != null && i < exprs.length; i++) {
             ExpressionImpl<?> e = (ExpressionImpl<?>)exprs[i];
             if (e != null) e.acceptVisit(visitor);
         }
     }


}
