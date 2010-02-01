package com.custardsource.parfait;

import java.util.Collection;

import org.springframework.context.Lifecycle;

import com.google.common.base.Preconditions;

/**
 * An output bridge for a particular set of Monitorables. A MonitoringView
 * provides a convenient lifecycle for an output destination to know when all
 * metrics have been set up and initialized, and are ready to be output to the
 * destination. When {@link #start()} is called, all Monitorables should be
 * initialized and ready to be written to the output. {@link #stop()} should be
 * (but, of course, is not guaranteed to be) called when the monitoring
 * subsystem is being shut down.
 */
public abstract class MonitoringView implements Lifecycle {
    private final MonitorableRegistry registry;
    private volatile boolean running = false;

    public MonitoringView(MonitorableRegistry registry) {
        this.registry = Preconditions.checkNotNull(registry);
    }

    /**
     * Starts monitoring all of the Monitorables contained withing the provided
     * {@link MonitorableRegistry}. Is permitted to (but is not required to)
     * 'freeze' the registry so that no further metrics can be added, if
     * post-start addition of {@link Monitorable Monitorables} is not supported
     * by the view.
     * 
     * @see org.springframework.context.Lifecycle#start()
     */
    public void start() {
        registry.freeze();
        startMonitoring(registry.getMonitorables());
        running = true;
    }

    protected abstract void startMonitoring(Collection<Monitorable<?>> monitorables);

    /**
     * Stops monitoring updates on the Monitorables in the provided registry.
     * 
     * @see org.springframework.context.Lifecycle#stop()
     */
    public void stop() {
        stopMonitoring(registry.getMonitorables());
        running = false;
    }

    protected abstract void stopMonitoring(Collection<Monitorable<?>> monitorables);

    /**
     * @return whether or not this view has been started with {@link #start()}
     */
    public boolean isRunning() {
        return running;
    }
}