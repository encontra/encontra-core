/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.inevo.encontra.index.search;


import pt.inevo.encontra.index.Index;
import pt.inevo.encontra.index.IndexEntry;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.ResultSet;
import pt.inevo.encontra.query.Query;
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
