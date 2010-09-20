package pt.inevo.encontra.query.criteria;

import java.lang.reflect.Field;

/**
 * Represents a simple or compound attribute path from a
 * bound type or collection, and is a "primitive" expression.
 *
 * @param <X>  the type of this path
 *
 * based on openjpa-persistence/src/main/java/org/apache/openjpa/persistence/criteria/PathImpl.java
 */
public class Path<X> extends ExpressionImpl<X> {
    protected final Path<?> _parent;
    protected final Field _field;
    
    protected Path(Class<X> cls) {
        super(cls);
        _parent = null;
        _field = null;
    }

    /**
     * Create a path from the given non-null parent representing the given non-null member. The given class denotes
     * the type expressed by this path.
     */
    public Path(Path<?> parent,Field field, Class<X> cls) {
        super(cls);
        _parent = parent;
        _field = field;
    }

    /**
     * Gets a new path that represents the attribute of the given name from this path.
     *
     * @exception IllegalArgumentException if this path represents a basic attribute that is can not be traversed
     * further.
     */
    public <Y> Path<Y> get(String attName) {
        Field field= null;
        try {
            field = getJavaType().getDeclaredField(attName);
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            return null;
        }
        Class fieldClass=field.getType();
        return new Path<Y>(this, field, fieldClass);
    }

     /**
     *  Return the parent "node" in the path or null if no parent.
     */
    public final Path<?> getParentPath() {
        return _parent;
    }
}
