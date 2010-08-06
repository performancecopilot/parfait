package com.custardsource.parfait;

import java.util.Collection;

import com.google.common.base.Preconditions;

public abstract class AbstractMonitoringView {
    private final MonitorableRegistry registry;
    private volatile boolean running = false;

    public AbstractMonitoringView(MonitorableRegistry registry) {
        this.registry = Preconditions.checkNotNull(registry);
    }

    public void start() {
        registry.freeze();
        startMonitoring(registry.getMonitorables());
        running = true;
    }

    protected abstract void startMonitoring(Collection<Monitorable<?>> monitorables);

    public void stop() {
        stopMonitoring(registry.getMonitorables());
        running = false;
    }

    protected abstract void stopMonitoring(Collection<Monitorable<?>> monitorables);

    public boolean isRunning() {
        return running;
    }
}