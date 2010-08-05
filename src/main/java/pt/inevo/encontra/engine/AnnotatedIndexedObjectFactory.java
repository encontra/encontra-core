package pt.inevo.encontra.engine;

import org.apache.commons.beanutils.PropertyUtils;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.IndexedObjectFactory;
import pt.inevo.encontra.index.IndexingException;
import pt.inevo.encontra.index.annotation.AnnotationUtils;
import pt.inevo.encontra.index.annotation.Indexed;
import pt.inevo.encontra.storage.IEntity;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public abstract class AnnotatedIndexedObjectFactory implements IndexedObjectFactory {

    @Override
    public List<IndexedObject> processBean(IEntity bean) throws IndexingException {
        List<IndexedField> fields=new ArrayList<IndexedField>();
        // iterate through fields
        for (final PropertyDescriptor property : PropertyUtils.getPropertyDescriptors(bean)) {
            final Method readMethod = property.getReadMethod();
            final Indexed annotation = (Indexed) AnnotationUtils.getAnnotation(readMethod, Indexed.class);
            if(readMethod !=null && annotation!=null){
                try {
                    final String propertyName=property.getName();
                    final Object prop = PropertyUtils.getProperty(bean,propertyName);
                    if (null == prop)
                        continue;
                    float boost = annotation.boost();
                    fields.add(new IndexedField(bean.getId(),propertyName, prop, boost));
                } catch (final Exception e) {
                    throw new IndexingException("Unable to index bean.", e);
                }
            }

        }

        return createObjects(fields);

    }

    protected abstract List<IndexedObject> createObjects(List<IndexedField> indexedFields);


}
