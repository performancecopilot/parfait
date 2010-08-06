package com.custardsource.parfait;

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
     * Starts monitoring all of the Monitorables contained withing the provided
     * {@link MonitorableRegistry}. Is permitted to (but is not required to)
     * 'freeze' the registry so that no further metrics can be added, if
     * post-start addition of {@link Monitorable Monitorables} is not supported
     * by the view.
     */
    void start();

    /**
     * Stops monitoring updates on the Monitorables in the provided registry.
     */
    void stop();

    /**
     * @return whether or not this view has been started with {@link #start()}
     */
    boolean isRunning();
}
