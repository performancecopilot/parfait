package io.pcp.parfait;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;

public class CompositeMonitoringView implements MonitoringView{

    private final List<MonitoringView> monitoringViews;

    public CompositeMonitoringView(MonitoringView... monitoringViews) {
        this.monitoringViews = copyOf(monitoringViews);
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
        return Iterators.any(monitoringViews.iterator(), new Predicate<MonitoringView>() {
            @Override
            public boolean apply(MonitoringView mv) {
                return mv.isRunning();
            }
        });
    }
}
