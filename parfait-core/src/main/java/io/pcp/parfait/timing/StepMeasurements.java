package io.pcp.parfait.timing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

public class StepMeasurements {
    private final StepMeasurements parent;
    private final List<StepMeasurements> children = new ArrayList<StepMeasurements>();
    private final List<MetricMeasurement> metricInstances = new ArrayList<MetricMeasurement>();

    private final String eventName;
    private final String action;
    private volatile boolean started = false;

    public StepMeasurements(StepMeasurements parent,
            String eventName, String action) {
        this.parent = parent;
        if (parent != null) {
            parent.addChildExecution(this);
        }
        this.eventName = eventName;
        this.action = action;
    }

    public StepMeasurements getParent() {
        return parent;
    }

    public void addMetricInstance(MetricMeasurement metric) {
        metricInstances.add(metric);
    }

    public void startAll() {
        for (MetricMeasurement metric : metricInstances) {
            metric.startTimer();
        }
        started = true;
    }

    public void stopAll() {
        started = false;
        for (MetricMeasurement metric : metricInstances) {
            metric.stopTimer();
        }
    }

    public void pauseAll() {
        for (MetricMeasurement metric : metricInstances) {
            metric.pauseOwnTime();
        }
    }

    public void resumeAll() {
        for (MetricMeasurement metric : metricInstances) {
            metric.resumeOwnTime();
        }
    }

    /**
     * @return a nicely-formatted list of all the events taken to reach the one under
     *         measurement (including that one as the last element)
     */
    String getBackTrace() {
        if (parent == null) {
            return stackTraceElement();
        }
        return parent.getBackTrace() + "/" + stackTraceElement();
    }

    /**
     * @return a nicely-formatted list of all the events invoked after the one under
     *         measurement (including that one as the first element)
     */
    String getForwardTrace() {
        if (children.isEmpty()) {
            return stackTraceElement();
        } else if (children.size() == 1) {
            return stackTraceElement() + "/" + children.get(0).getForwardTrace();
        } else {
            // Handles the 'freak case' where one event may forward directly to MORE than one
            // 'child'. I have no idea if this ever happens, but we might as well handle it.
            List<String> childTraces = new ArrayList<String>(children.size());
            for (StepMeasurements child : children) {
                childTraces.add(child.getForwardTrace());
            }
            return stackTraceElement() + "/{" + Joiner.on('|').join(childTraces) + "}";
        }
    }

    private void addChildExecution(StepMeasurements newTiming) {
        children.add(newTiming);
    }

    private String stackTraceElement() {
        return eventName + (Strings.isNullOrEmpty(action) ? "" : ":" + action);
    }

    String getEventName() {
        return eventName;
    }

    public Collection<MetricMeasurement> getMetricInstances() {
        return metricInstances;
    }

    public Map<ThreadMetric, Long> snapshotValues() {
        if (!started) {
            return Collections.emptyMap();
        }
        Collection<MetricMeasurement> snapshot = new ArrayList<MetricMeasurement>(metricInstances);
        Map<ThreadMetric, Long> results = new HashMap<ThreadMetric, Long>();
        for (MetricMeasurement measurement : snapshot) {
            results.put(measurement.getMetricSource(), measurement.inProgressValue());
        }
        return results;
    }

}
