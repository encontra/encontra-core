package pt.inevo.encontra.index;

import pt.inevo.encontra.descriptors.EncontraDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Encontra IndexEntry Builder
 */
public interface IndexEntryBuilder<O extends AbstractObject,E extends IndexEntry> {

    public abstract E createIndexEntry(O object);
}
