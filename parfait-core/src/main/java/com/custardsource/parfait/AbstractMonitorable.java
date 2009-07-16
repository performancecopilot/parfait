package com.custardsource.parfait;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

/**
 * Convenience base class for implementing {@link Monitorable}. Provides implementations for
 * meta-data methods and a high performance synchronization free implementation of {@link Monitor}
 * notification.
 */
public abstract class AbstractMonitorable<T> implements Monitorable<T> {

    protected final Logger LOG;
    
    private final List<Monitor> monitors = new CopyOnWriteArrayList<Monitor>();

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
            monitors.add(monitor);
        }
    }

    public synchronized void removeMonitor(Monitor monitor) {
        if (isAttached(monitor)) {
            monitors.remove(monitor);
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
        logValue();
        for (Monitor monitor : monitors) {
            monitor.valueChanged(this);
        }
    }
        
    protected abstract void logValue();
}
