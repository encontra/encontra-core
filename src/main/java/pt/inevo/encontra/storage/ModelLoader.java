package pt.inevo.encontra.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract loader.
 * @author jpvguerreiro
 */
public abstract class ModelLoader<I extends IEntity, O extends Object> implements Iterable<File> {

    protected String modelsPath = "";
    protected List<File> modelsFiles;

    public ModelLoader() {
    }

    public ModelLoader(String modelsPath) {
        this.modelsPath = modelsPath;
    }

    public abstract I loadModel(File model);

    public abstract O loadBuffered(File model);

    public abstract List<I> getModels(String path);

    public abstract void load(String path);

    public void load() {
        load(modelsPath);
    }

    public List<I> getModels() {
        return getModels(modelsPath);
    }

    @Override
    public Iterator<File> iterator() {
        return modelsFiles.iterator();
    }

    public String getModelsPath() {
        return modelsPath;
    }

    public void setModelsPath(String modelsPath) {
        this.modelsPath = modelsPath;
    }
}
