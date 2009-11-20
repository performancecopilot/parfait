package com.custardsource.parfait;

import java.util.Collection;

import org.springframework.context.Lifecycle;

import com.google.common.base.Preconditions;

public abstract class MonitoringView implements Lifecycle {
    private final MonitorableRegistry registry;
    private volatile boolean running = false;


    public MonitoringView(MonitorableRegistry registry) {
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