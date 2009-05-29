package com.custardsource.parfait;

import org.apache.log4j.Logger;

/**
 * Convenience base class for implementing {@link Monitorable}. Provides implementations for
 * meta-data methods and a high performance synchronization free implementation of {@link Monitor}
 * notification.
 */
public abstract class AbstractMonitorable<T> implements Monitorable<T> {

    protected final Logger LOG;
    
    private final Monitor[] NO_MONITORS = {};

    private volatile Monitor[] monitors = NO_MONITORS;

    private final String name;

    private final String description;

    private final Class<T> type;

    public AbstractMonitorable(String name, String description, Class<T> type) {
        this.name = name;
        this.description = description;
        this.type = type;
        LOG = Logger.getLogger("pcp."+name);
    }

    protected void registerSelf(MonitorableRegistry registry) {
        registry.register(this);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Class<T> getType() {
        return type;
    }

    public synchronized void attachMonitor(Monitor monitor) {
        if (!isAttached(monitor)) {
            Monitor[] newMonitors = new Monitor[monitors.length + 1];
            System.arraycopy(monitors, 0, newMonitors, 0, monitors.length);
            newMonitors[monitors.length] = monitor;
            monitors = newMonitors;
        }
    }

    public synchronized void removeMonitor(Monitor monitor) {
        if (isAttached(monitor)) {
            Monitor[] newMonitors = new Monitor[monitors.length - 1];
            for (int i = 0, j = 0; i < monitors.length; i++) {
                if (monitors[i] != monitor) {
                    newMonitors[j++] = monitors[i];
                }
            }
            monitors = newMonitors;
        }
    }

    private boolean isAttached(Monitor monitorToFind) {
        for (Monitor monitor : monitors) {
            if (monitor.equals(monitorToFind)) {
                return true;
            }
        }
        return false;
    }

    protected void notifyMonitors() {
        // Because we're not synchronized here we need to copy the current version of monitors as
        // it's possible it could be changed while the following loop is executing.
        logValue();
        Monitor[] monitorsCopy = monitors;
        for (Monitor monitor : monitorsCopy) {
            monitor.valueChanged(this);
        }
    }
        
    protected abstract void logValue();
}
