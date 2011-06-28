package pt.inevo.encontra.test.entities;

import pt.inevo.encontra.index.annotation.Indexed;
import pt.inevo.encontra.storage.IEntity;

/**
 * Indexable fields are marked with the @Indexed annotation
 * @author Ricardo
 */
public class MetaTestModel implements IEntity<Long> {

    private Long id;
    private String title;
    private String content;
    private String description;

    public MetaTestModel(String title, String content) {
        this.title = title;
        this.content = content;
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
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Indexed
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "MetaTestModel{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", content='" + content + '\''
                + '}';
    }

    @Indexed
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
