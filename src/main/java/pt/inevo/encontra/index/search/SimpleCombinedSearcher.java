package pt.inevo.encontra.index.search;

import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.query.SimpleQueryCombiner;

/**
 *
 */
public class SimpleCombinedSearcher<E extends IndexedObject> extends CombinedSearcher<E>{

    public SimpleCombinedSearcher(){
        super();
        setQueryCombiner(new SimpleQueryCombiner());
    }
}