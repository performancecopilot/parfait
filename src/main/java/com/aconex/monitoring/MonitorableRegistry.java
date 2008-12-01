package com.aconex.monitoring;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.aconex.utilities.Assert;

public class MonitorableRegistry {

    /**
     * This is a TreeMap so that the monitorables a maintained in alphabetical order.
     */
    private static final Map<String, Monitorable<?>> monitorables = new TreeMap<String, Monitorable<?>>();

    private static boolean isMonitorBridgeStarted = false;

    public static synchronized <T> void register(Monitorable<T> monitorable) {
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

    /**
     * Useful only in Unit tests
     */
    public static void shutdown() {
        if (isMonitorBridgeStarted) {
            isMonitorBridgeStarted = false;
        }
        /*
         * We clear this anyway, because unit tests may have registered Monitorables even though the Registry
         * is not started.  Without this, subsequent unit tests might fail when they try to register the Monitorable again.
         */
        monitorables.clear();

    }

    public static synchronized Collection<Monitorable<?>> getMonitorables() {
        Assert.isFalse(isMonitorBridgeStarted, "PCP Monitor Bridge should have not been started!");
        isMonitorBridgeStarted = true;
        return Collections.unmodifiableCollection(monitorables.values());
    }

}
