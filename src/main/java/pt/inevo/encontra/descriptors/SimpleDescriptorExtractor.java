package pt.inevo.encontra.descriptors;

import pt.inevo.encontra.index.IndexedObject;

public class SimpleDescriptorExtractor extends DescriptorExtractor<IndexedObject,SimpleDescriptor> {

    public SimpleDescriptorExtractor(Class descriptorClass){
        super(IndexedObject.class,descriptorClass);
    }

    @Override
    public SimpleDescriptor extract(IndexedObject object) {
        SimpleDescriptor descriptor= newDescriptor();
        descriptor.setId(object.getId());
        descriptor.setValue(object.getValue());
        return descriptor;
    }

    @Override
    protected IndexedObject setupIndexedObject(SimpleDescriptor descriptor, IndexedObject object){
        object.setId(descriptor.getId());
        object.setValue(descriptor.getValue());
        return object;
    }
}

