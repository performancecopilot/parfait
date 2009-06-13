package com.custardsource.parfait;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.Preconditions;

public class MonitorableRegistry {
	public static MonitorableRegistry DEFAULT_REGISTRY = new MonitorableRegistry();
	
    /**
     * This is a TreeMap so that the Monitorables are maintained in alphabetical order.
     */
    private final Map<String, Monitorable<?>> monitorables = new TreeMap<String, Monitorable<?>>();

    private boolean stateFrozen = false;

    public synchronized <T> void register(Monitorable<T> monitorable) {
        if (stateFrozen) {
            throw new IllegalStateException("Cannot register monitorable " + monitorable.getName()
                    + " after MonitorableRegistry has been frozen");
        }
        if (monitorables.containsKey(monitorable.getName())) {
            throw new UnsupportedOperationException(
                    "There is already an instance of the Monitorable [" + monitorable.getName()
                            + "] registered.");
        }
        monitorables.put(monitorable.getName(), monitorable);
    }

    public synchronized void freeze() {
    	stateFrozen = true;
    }
    
    public synchronized Collection<Monitorable<?>> getMonitorables() {
    	Preconditions.checkState(stateFrozen, "MonitorableRegistry must be frozen before retrieving monitorable list");
        stateFrozen = true;
        return Collections.unmodifiableCollection(monitorables.values());
    }
    
    /*
     * Testing only -- should be eliminated once the default registry is gone
     */
    public static void clearDefaultRegistry() {
        DEFAULT_REGISTRY = new MonitorableRegistry();
    }
}