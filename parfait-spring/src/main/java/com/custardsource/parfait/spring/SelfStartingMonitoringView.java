package com.custardsource.parfait.spring;

import com.custardsource.parfait.*;
import org.springframework.context.Lifecycle;

/**
 * Adapter between a normal MonitoringView and Spring's Lifecycle interface (which conveniently has the exact-same
 * methods).
 */
public class SelfStartingMonitoringView implements Lifecycle {

    private final MonitoringViewDelegate monitoringViewDelegate;

    public SelfStartingMonitoringView(MonitoringViewDelegate monitoringViewDelegate) {
        this.monitoringViewDelegate = monitoringViewDelegate;
    }

    /**
     * @deprecated,
     * Use <code>SelfStartingMonitoringView(MonitoringViewDelegate monitoringViewDelegate)</code> instead.
     */
    @Deprecated
    public SelfStartingMonitoringView(MonitoringView monitoringView) {
        monitoringViewDelegate = new MonitoringViewDelegate(monitoringView);
    }

    /**
     * @deprecated,
     * Use <code>SelfStartingMonitoringView(MonitoringViewDelegate monitoringViewDelegate)</code> instead.
     */
    @Deprecated
    public SelfStartingMonitoringView(MonitoringView monitoringView, final long quietPeriodInMillis) {
        monitoringViewDelegate = new MonitoringViewDelegate(monitoringView, quietPeriodInMillis);
    }

    /**
     * @deprecated,
     * Use <code>SelfStartingMonitoringView(MonitoringViewDelegate monitoringViewDelegate)</code> instead.
     */
    @Deprecated
    public SelfStartingMonitoringView(MonitorableRegistry registry, MonitoringView monitoringView) {
        monitoringViewDelegate = new MonitoringViewDelegate(registry, monitoringView);
    }

    /**
     * @deprecated,
     * Use <code>SelfStartingMonitoringView(MonitoringViewDelegate monitoringViewDelegate)</code> instead.
     */
    @Deprecated
    public SelfStartingMonitoringView(MonitorableRegistry registry, MonitoringView monitoringView, final long quietPeriodInMillis) {
        monitoringViewDelegate = new MonitoringViewDelegate(registry, monitoringView, quietPeriodInMillis);
    }

    @Override
    public void start() {
        monitoringViewDelegate.start();
    }

    @Override
    public void stop() {
        monitoringViewDelegate.stop();
    }

    @Override
    public boolean isRunning() {
        return monitoringViewDelegate.isRunning();
    }

    public static final long defaultQuietPeriod() {
        return MonitoringViewDelegate.defaultQuietPeriod();
    }
}
