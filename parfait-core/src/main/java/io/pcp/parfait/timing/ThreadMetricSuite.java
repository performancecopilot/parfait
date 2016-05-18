package io.pcp.parfait.timing;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ThreadMetricSuite {
    private final List<ThreadMetric> metrics;
    
    private ThreadMetricSuite(Collection<? extends ThreadMetric> metrics) {
        this.metrics = new CopyOnWriteArrayList<ThreadMetric>(metrics);
    }
    
    public final void addMetric(ThreadMetric metric) {
        metrics.add(metric);
    }

    public final void addAllMetrics(Collection<ThreadMetric> metrics) {
        this.metrics.addAll(metrics);
    }
    
    public final List<ThreadMetric> metrics() {
        return Collections.unmodifiableList(metrics);
    }

    public static ThreadMetricSuite blank() {
        return new ThreadMetricSuite(Collections.<ThreadMetric>emptyList());
    }

    public static ThreadMetricSuite withDefaultMetrics() {
        return new ThreadMetricSuite(StandardThreadMetrics.defaults());
    }
}
