/**
 * 
 */
package com.custardsource.parfait.timing;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class MonitoredCounterSet {
    private final Map<ThreadMetric, ControllerCounterSet> metrics = new LinkedHashMap<ThreadMetric, ControllerCounterSet>();
    private final ControllerCounterSet invocationCounter;

    public MonitoredCounterSet(ControllerCounterSet invocationCounter) {
        this.invocationCounter = invocationCounter;
    }

    public ControllerCounterSet getInvocationCounter() {
        return invocationCounter;
    }

    public void addMetric(ThreadMetric metric) {
        addMetric(metric, null);
    }

    public void addMetric(ThreadMetric metric, ControllerCounterSet counter) {
        metrics.put(metric, counter);
    }

    Collection<ThreadMetric> getMetricSources() {
        return metrics.keySet();
    }

    ControllerCounterSet getCounterForMetric(ThreadMetric metric) {
        return metrics.get(metric);
    }

    Integer numberOfControllerCounters() {
        return metrics.values().size();
    }

    Map<ThreadMetric, ControllerCounterSet> getMetrics() {
        return Collections.unmodifiableMap(metrics);
    }
}