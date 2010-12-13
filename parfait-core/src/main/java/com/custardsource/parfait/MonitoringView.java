package com.custardsource.parfait;

import java.util.Collection;

/**
 * An output bridge for a particular set of Monitorables. A MonitoringView
 * provides a convenient lifecycle for an output destination to know when all
 * metrics have been set up and initialized, and are ready to be output to the
 * destination. When {@link #start()} is called, all Monitorables should be
 * initialized and ready to be written to the output. {@link #stop()} should be
 * (but, of course, is not guaranteed to be) called when the monitoring
 * subsystem is being shut down.
 */
public interface MonitoringView {

    /**
     * TODO properly document
     * Begins, or resets the monitoring view state, if startMonitoring has been previously called
     * then the owner of this instance should call stopMonitoring first.
     */
    void startMonitoring(Collection<Monitorable<?>> monitorables);

    /**
     * Stops monitoring updates on the Monitorables in the provided registry.
     */
    void stopMonitoring(Collection<Monitorable<?>> monitorables);

    /**
     * @return whether or not this view has been started with {@link #startMonitoring(java.util.Collection)} ()}
     */
    boolean isRunning();
}
