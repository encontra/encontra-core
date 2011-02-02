package pt.inevo.encontra.common;

import java.util.ArrayList;
import java.util.List;

/**
 * SyncResultProvider. Actually it's just being used as a proxy interface.
 * @author Ricardo
 */
public class SyncResultProvider implements ResultProvider {

    protected ResultSet resultSet;
    protected List<ResultSetListener> listeners;

    public SyncResultProvider() {
        listeners = new ArrayList<ResultSetListener>();
    }

    public SyncResultProvider(ResultSet set) {
        listeners = new ArrayList<ResultSetListener>();
        this.resultSet = set;
        this.resultSet.registerListener(this);
    }

    @Override
    public void handleEvent(ResultSetEvent event) {
        for (ResultSetListener l : listeners) {
            l.handleEvent(event);
        }
    }

    @Override
    public boolean registerListener(ResultSetListener listener){
        return listeners.add(listener);
    }

    @Override
    public ResultSet getResultSet() {
        return resultSet;
    }

    @Override
    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
        this.resultSet.registerListener(this);
    }
}
