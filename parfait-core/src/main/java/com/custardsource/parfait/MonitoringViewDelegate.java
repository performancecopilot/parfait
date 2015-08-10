package com.custardsource.parfait;

import com.google.common.collect.Lists;

import java.util.Collection;

public class MonitoringViewDelegate {

    private static final long DEFAULT_QUIET_PERIOD = 5000L;
    private final long quietPeriodInMillis;
    private final MonitoringView monitoringView;
    private final MonitorableRegistry monitorableRegistry;
    private Collection<Monitorable<?>> previouslySeenMonitorables = Lists.newArrayList();
    private QuiescentRegistryListener quiescentRegistryListener;

    public MonitoringViewDelegate(MonitoringView monitoringView) {
        this(monitoringView, DEFAULT_QUIET_PERIOD);
    }

    public MonitoringViewDelegate(MonitoringView monitoringView, final long quietPeriodInMillis) {
        this(MonitorableRegistry.DEFAULT_REGISTRY, monitoringView, quietPeriodInMillis);
    }

    public MonitoringViewDelegate(MonitorableRegistry registry, MonitoringView monitoringView) {
        this(registry, monitoringView, DEFAULT_QUIET_PERIOD);
    }

    public MonitoringViewDelegate(MonitorableRegistry registry, MonitoringView monitoringView, final long quietPeriodInMillis) {
        this.monitoringView = monitoringView;
        this.monitorableRegistry = registry;
        this.quietPeriodInMillis = quietPeriodInMillis;
    }

    // TODO Pass in a MonitorableRegistryListener in a constructor so we can make unit tests even easier.
    public void start() {
        previouslySeenMonitorables = monitorableRegistry.getMonitorables();
        monitoringView.startMonitoring(previouslySeenMonitorables);
        this.quiescentRegistryListener = new QuiescentRegistryListener(new Runnable() {
            @Override
            public void run() {
                stop();
                start();
            }
        }, quietPeriodInMillis);
        monitorableRegistry.addRegistryListener(quiescentRegistryListener);
    }

    public void stop() {
        monitorableRegistry.removeRegistryListener(quiescentRegistryListener);
        this.quiescentRegistryListener.stop();
        monitoringView.stopMonitoring(previouslySeenMonitorables);
    }

    public boolean isRunning() {
        return monitoringView.isRunning();
    }

    public static final long defaultQuietPeriod() {
        return DEFAULT_QUIET_PERIOD;
    }
}
