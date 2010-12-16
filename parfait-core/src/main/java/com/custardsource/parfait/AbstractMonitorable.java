package com.custardsource.parfait;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.measure.unit.Unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience base class for implementing {@link Monitorable}. Provides implementations for
 * metadata methods and a high performance synchronization-free implementation of {@link Monitor}
 * notification.
 */
public abstract class AbstractMonitorable<T> implements Monitorable<T> {
    protected final Logger LOG;
    
    private final List<Monitor> monitors = new CopyOnWriteArrayList<Monitor>();
    private final String name;
    private final String description;
    private final Class<T> type;
    private final Unit<?> unit;
    private final ValueSemantics semantics;

    protected AbstractMonitorable(String name, String description, Class<T> type, Unit<?> unit, ValueSemantics semantics) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.unit = unit;
        this.semantics = semantics;
        LOG = LoggerFactory.getLogger("parfait."+name);
    }

    protected void registerSelf(MonitorableRegistry registry) {
        registry.register(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public Class<T> getType() {
        return type;
    }
    
    @Override
    public Unit<?> getUnit() {
        return unit;
    }
    
    @Override
    public ValueSemantics getSemantics() {
        return semantics;
    }

    @Override
    public synchronized void attachMonitor(Monitor monitor) {
        if (!isAttached(monitor)) {
            monitors.add(monitor);
        }
    }

    @Override
    public synchronized void removeMonitor(Monitor monitor) {
        if (isAttached(monitor)) {
            monitors.remove(monitor);
        }
    }

    /**
     * Checks if a given Monitor is registered for notifications with this Monitorable.  
     * @return true if the monitor is currently in the notification list for the monitorable
     * 
     */
    synchronized boolean isAttached(Monitor monitorToFind) {
        return monitors.contains(monitorToFind);
    }

    protected final void notifyMonitors() {
        logValue();
        for (Monitor monitor : monitors) {
            monitor.valueChanged(this);
        }
    }

    protected final void logValue() {
        if (LOG.isTraceEnabled()) {
            LOG.trace(getName() + "=" + get());
        }
    }
}
