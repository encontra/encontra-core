package pt.inevo.encontra.test.searchers;

import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.descriptors.SimpleDescriptor;
import pt.inevo.encontra.index.SimpleIndex;
import pt.inevo.encontra.index.search.ParallelSimpleSearcher;
import pt.inevo.encontra.storage.SimpleObjectStorage;
import pt.inevo.encontra.test.entities.ExampleDescriptor;
import pt.inevo.encontra.test.entities.StringObject;

/**
 * Testing the ParallelSimpleSearcher.
 *
 * @author ricardo
 */
public class ParallelSimpleSearcherTest extends AbstractSearcherTest {

    public ParallelSimpleSearcherTest(String testName) {
        super(testName);
    }

    /**
     * An example of a Descriptor extractor
     */
    public class TestDescriptorExtractor extends DescriptorExtractor<StringObject, SimpleDescriptor> {

        public TestDescriptorExtractor(Class descriptorClass) {
            super(StringObject.class, descriptorClass);
        }

        @Override
        public SimpleDescriptor extract(StringObject object) {
            SimpleDescriptor descriptor = newDescriptor();
            descriptor.setId(object.getId());
            descriptor.setValue(object.getValue());
            return descriptor;
        }

        @Override
        protected StringObject setupIndexedObject(SimpleDescriptor descriptor, StringObject object) {
            object.setId((Long) descriptor.getId());
            object.setValue(descriptor.getValue());
            return object;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //creating a simple multi-descriptor extractor
        DescriptorExtractor descriptorExtractor = new TestDescriptorExtractor(ExampleDescriptor.class);

        //Creating the searchers - A searcher for the "title"
        searcher = new ParallelSimpleSearcher<StringObject>();
        searcher.setObjectStorage(new SimpleObjectStorage(StringObject.class));
        searcher.setDescriptorExtractor(descriptorExtractor);
        searcher.setIndex(new SimpleIndex(ExampleDescriptor.class));

        //Inserting some elements into the engine (indexes)
        for (int i = 0; i < 1000; i++)
            searcher.insert(new StringObject(Integer.toString(i)));
    }
}