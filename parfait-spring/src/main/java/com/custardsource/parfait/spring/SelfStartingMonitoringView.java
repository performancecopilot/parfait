package com.custardsource.parfait.spring;

import com.custardsource.parfait.MonitoringView;
import org.springframework.context.Lifecycle;

/**
 * Adapter between a normal MonitoringView and Spring's Lifecycle interface (which conveniently has the exact-same
 * methods).
 */
public class SelfStartingMonitoringView implements MonitoringView, Lifecycle {
    private final MonitoringView wrappedView;

    public SelfStartingMonitoringView(MonitoringView wrappedView) {
        this.wrappedView = wrappedView;
    }

    @Override
    public void start() {
        wrappedView.start();
    }

    @Override
    public void stop() {
        wrappedView.stop();
    }

    @Override
    public boolean isRunning() {
        return wrappedView.isRunning();
    }
}
