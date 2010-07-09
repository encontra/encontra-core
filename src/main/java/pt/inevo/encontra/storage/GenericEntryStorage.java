package pt.inevo.encontra.storage;

import pt.inevo.encontra.index.AbstractObject;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


public abstract class GenericEntryStorage<I extends Serializable,E extends IEntry<I,?>>implements EntryStorage<I,E>{
    Class<E> clazz;
    /**
     * Default constructor. Use for extend this class.
     */
    @SuppressWarnings(value = "unchecked")
    public GenericEntryStorage() {
        /*
        Type[] types = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();

        if (types[0] instanceof ParameterizedType) {
            // If the class has parameterized types, it takes the raw type.
            ParameterizedType type = (ParameterizedType) types[0];
            clazz = (Class<IEntry>) type.getRawType();
        } else {
            clazz = (Class<IEntry>) types[0];
        } */
        
    }

    /**
     * Constructor with given {@link AbstractObject} implementation. Use for creting DAO without extending
     * this class.
     *
     * @param clazz class with will be accessed by DAO methods
     */
    @SuppressWarnings(value = "unchecked")
    public GenericEntryStorage(Class<E> clazz) {
        this.clazz = clazz;
    }

    protected E newEntryValue(){
        try {
            return (E) getEntryClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace(); 
        }
        return null;
    }
    
    protected Class<E> getEntryClass() {
        return clazz;
    }
}
