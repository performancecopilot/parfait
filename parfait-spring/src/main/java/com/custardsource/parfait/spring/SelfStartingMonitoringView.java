package com.custardsource.parfait.spring;

import com.custardsource.parfait.DynamicMonitoringView;
import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.MonitoringView;
import org.springframework.context.Lifecycle;

/**
 * Adapter between a normal MonitoringView and Spring's Lifecycle interface (which conveniently has the exact-same
 * methods).
 */
public class SelfStartingMonitoringView implements Lifecycle {

    private final DynamicMonitoringView dynamicMonitoringView;

    public SelfStartingMonitoringView(DynamicMonitoringView dynamicMonitoringView) {
        this.dynamicMonitoringView = dynamicMonitoringView;
    }

    /**
     * @deprecated,
     * Use <code>SelfStartingMonitoringView(DynamicMonitoringView dynamicMonitoringView)</code> instead.
     */
    @Deprecated
    public SelfStartingMonitoringView(MonitoringView monitoringView) {
        dynamicMonitoringView = new DynamicMonitoringView(monitoringView);
    }

    /**
     * @deprecated,
     * Use <code>SelfStartingMonitoringView(DynamicMonitoringView dynamicMonitoringView)</code> instead.
     */
    @Deprecated
    public SelfStartingMonitoringView(MonitoringView monitoringView, final long quietPeriodInMillis) {
        dynamicMonitoringView = new DynamicMonitoringView(monitoringView, quietPeriodInMillis);
    }

    /**
     * @deprecated,
     * Use <code>SelfStartingMonitoringView(DynamicMonitoringView dynamicMonitoringView)</code> instead.
     */
    @Deprecated
    public SelfStartingMonitoringView(MonitorableRegistry registry, MonitoringView monitoringView) {
        dynamicMonitoringView = new DynamicMonitoringView(registry, monitoringView);
    }

    /**
     * @deprecated,
     * Use <code>SelfStartingMonitoringView(DynamicMonitoringView dynamicMonitoringView)</code> instead.
     */
    @Deprecated
    public SelfStartingMonitoringView(MonitorableRegistry registry, MonitoringView monitoringView, final long quietPeriodInMillis) {
        dynamicMonitoringView = new DynamicMonitoringView(registry, monitoringView, quietPeriodInMillis);
    }

    @Override
    public void start() {
        dynamicMonitoringView.start();
    }

    @Override
    public void stop() {
        dynamicMonitoringView.stop();
    }

    @Override
    public boolean isRunning() {
        return dynamicMonitoringView.isRunning();
    }

    public static final long defaultQuietPeriod() {
        return DynamicMonitoringView.defaultQuietPeriod();
    }
}
