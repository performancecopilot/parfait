package com.custardsource.parfait;

import javax.measure.unit.Unit;

/**
 * The Monitorable interface must be implemented by any value that needs to be monitored using the
 * Parfait monitoring system.
 *
 * @see MonitoredValue
 * @see MonitoredCounter
 */
public interface Monitorable<T> {

    /**
     * The name of this Monitorable. Name must be unique across a single JVM.
     */
    String getName();

    String getDescription();
    
    Unit<?> getUnit();
    
    ValueSemantics getSemantics();
    

    /**
     * The type of the value returned by the {@link #get()} method.
     */
    Class<T> getType();

    /**
     * Returns the current value of this Monitorable.
     * <p>
     * This method should never block and must return as fast as is possible.
     */
    T get();

    /**
     * Attaches the provided Monitor. Once attached the Monitor will be notified whenever the value
     * of this Monitorable changes.
     */
    void attachMonitor(Monitor m);

    /**
     * Removed the provided Monitor from the list of attached Monitors.
     */
    void removeMonitor(Monitor m);
}