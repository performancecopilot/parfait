/**
 * 
 */
package com.custardsource.parfait.timing;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class EventCounters {
    private final Map<ThreadMetric, EventMetricCounters> metrics = new LinkedHashMap<ThreadMetric, EventMetricCounters>();
    private final EventMetricCounters invocationCounter;

    public EventCounters(EventMetricCounters invocationCounter) {
        this.invocationCounter = invocationCounter;
    }

    public EventMetricCounters getInvocationCounter() {
        return invocationCounter;
    }

    public void addMetric(ThreadMetric metric) {
        addMetric(metric, null);
    }

    public void addMetric(ThreadMetric metric, EventMetricCounters counter) {
        metrics.put(metric, counter);
    }

    Collection<ThreadMetric> getMetricSources() {
        return metrics.keySet();
    }

    EventMetricCounters getCounterForMetric(ThreadMetric metric) {
        return metrics.get(metric);
    }

    Integer numberOfControllerCounters() {
        return metrics.values().size();
    }

    Map<ThreadMetric, EventMetricCounters> getMetrics() {
        return Collections.unmodifiableMap(metrics);
    }
}