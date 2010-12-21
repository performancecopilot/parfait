package com.custardsource.parfait;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import java.util.Collection;

public class CompositeMonitoringView implements MonitoringView{

    private final MonitoringView[] monitoringViews;

    public CompositeMonitoringView(MonitoringView... monitoringViews) {
        this.monitoringViews = monitoringViews;
    }

    @Override
    public void startMonitoring(Collection<Monitorable<?>> monitorables) {
        for (MonitoringView monitoringView : monitoringViews) {
            monitoringView.startMonitoring(monitorables);
        }
    }

    @Override
    public void stopMonitoring(Collection<Monitorable<?>> monitorables) {
        for (MonitoringView monitoringView : monitoringViews) {
            monitoringView.stopMonitoring(monitorables);
        }
    }

    @Override
    /**
     * For a Composite view, isRunning() is true if any of the underlying views are still running
     */
    public boolean isRunning() {
        return Iterators.any(Iterators.forArray(monitoringViews), new Predicate<MonitoringView>() {
            @Override
            public boolean apply(MonitoringView mv) {
                return mv.isRunning();
            }
        });
    }
}
