package com.custardsource.parfait.spring;

import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.MonitoringView;
import com.custardsource.parfait.QuiescentRegistryListener;
import com.google.common.collect.Lists;
import org.springframework.context.Lifecycle;

import java.util.Collection;

/**
 * Adapter between a normal MonitoringView and Spring's Lifecycle interface (which conveniently has the exact-same
 * methods).
 */
public class SelfStartingMonitoringView implements Lifecycle {

    private final MonitoringView monitoringView;
    private final MonitorableRegistry monitorableRegistry;
    private Collection<Monitorable<?>> previouslySeenMonitorables = Lists.newArrayList();
    private static final long DEFAULT_QUIET_PERIOD = 5000L;
    private QuiescentRegistryListener quiescentRegistryListener;
    private final long quietPeriodInMillis;


    public SelfStartingMonitoringView(MonitoringView monitoringView) {
        this(monitoringView, DEFAULT_QUIET_PERIOD);
    }

    public SelfStartingMonitoringView(MonitoringView monitoringView, final long quietPeriodInMillis) {
        this(MonitorableRegistry.DEFAULT_REGISTRY, monitoringView, quietPeriodInMillis);
    }

    public SelfStartingMonitoringView(MonitorableRegistry registry, MonitoringView monitoringView, final long quietPeriodInMillis) {
        this.monitoringView = monitoringView;
        this.monitorableRegistry = registry;
        this.quietPeriodInMillis = quietPeriodInMillis;
    }

    // TODO Pass in a MonitorableRegistryListener in a constructor so we can make unit tests even easier.  
    @Override
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

    @Override
    public void stop() {
        monitorableRegistry.removeRegistryListener(quiescentRegistryListener);
        this.quiescentRegistryListener.stop();

        if (!previouslySeenMonitorables.isEmpty()) {
            monitoringView.stopMonitoring(previouslySeenMonitorables);
        }
    }

    @Override
    public boolean isRunning() {
        return monitoringView.isRunning();
    }
}
