package com.aconex.monitoring;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.aconex.utilities.Assert;

public class MonitorableRegistry {
	public static final MonitorableRegistry DEFAULT_REGISTRY = new MonitorableRegistry();
	
    /**
     * This is a TreeMap so that the monitorables a maintained in alphabetical order.
     */
    private final Map<String, Monitorable<?>> monitorables = new TreeMap<String, Monitorable<?>>();

    private boolean isMonitorBridgeStarted = false;

    public synchronized <T> void register(Monitorable<T> monitorable) {
        if (isMonitorBridgeStarted) {
            throw new IllegalStateException("Cannot register monitorable " + monitorable.getName()
                    + " after MonitorBridge has been started");
        }
        if (monitorables.containsKey(monitorable.getName())) {
            throw new UnsupportedOperationException(
                    "There is already an instance of the Monitorable [" + monitorable.getName()
                            + "] registered.");
        }
        monitorables.put(monitorable.getName(), monitorable);
    }

    public synchronized Collection<Monitorable<?>> getMonitorables() {
        Assert.isFalse(isMonitorBridgeStarted, "PCP Monitor Bridge should have not been started!");
        isMonitorBridgeStarted = true;
        return Collections.unmodifiableCollection(monitorables.values());
    }
}