package pt.inevo.encontra.test.entities;

import pt.inevo.encontra.index.annotation.Indexed;
import pt.inevo.encontra.storage.IEntity;

public class CompoundMetaTestModel implements IEntity<Long> {

    private Long id;
    private String name;
    private MetaTestModel testModel;

    public CompoundMetaTestModel(String name, MetaTestModel content) {
        this.name = name;
        this.testModel = content;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Indexed
    public String getName() {
        return name;
    }

    public void setTitle(String name) {
        this.name = name;
    }

    @Indexed
    public MetaTestModel getTestModel() {
        return testModel;
    }

    public void setTestModel(MetaTestModel content) {
        this.testModel = content;
    }

    @Override
    public String toString() {
        return "CompoundMetaTestModel{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", content='" + testModel.toString() + '\''
                + '}';
    }
}
