package com.custardsource.parfait.timing;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * <p>
 * Coordinates multiple {@link MetricMeasurement MetricMeasurements} for all the events invoked in
 * the process of handling the user request.
 * </p>
 * <p>
 * Not thread-safe, should be used only by one thread at a time (obtaining the top-level measurement
 * via {@link #getInProgressMeasurements()} is thread-safe and permitted).
 * </p>
 */
public class EventMetricCollector {
    private volatile StepMeasurements top = null;

    private StepMeasurements current = null;
    /**
     * The number of nested events invoked so far. When we hit depth of 0 we know we've reached
     * the top-level event requested by the user.
     */
    private int depth = 0;
    private Object topLevelEvent;

    private final Map<Object, EventCounters> perEventCounters;

    private static final Logger LOG = Logger.getLogger(EventMetricCollector.class);

    public EventMetricCollector(Map<Object, EventCounters> perEventCounters) {
        this.perEventCounters = perEventCounters;
    }

    public void startTiming(Object eventGroup, String event) {
        StepMeasurements newTiming = new StepMeasurements(current, eventGroup.getClass(),
                event);
        for (ThreadMetric metric : perEventCounters.get(eventGroup).getMetricSources()) {
            newTiming.addMetricInstance(new MetricMeasurement(metric, Thread.currentThread()));
        }
        current = newTiming;
        topLevelEvent = eventGroup;
        depth++;
        if (top == null) {
            top = newTiming;
        }
        current.startAll();
    }

    public void stopTiming() {
        current.stopAll();
        depth--;
        String depthText = (depth > 0) ? "Nested (" + depth + ")" : "Top";

        String metricData = "";
        for (MetricMeasurement metric : current.getMetricInstances()) {
            metricData += "\t" + metric.getMetricName() + ": own " +
                    metric.ownTimeValueFormatted() + ", total " + metric.totalValueFormatted();
        }
        LOG.info(String.format("%s\t%s\t%s\t%s", depthText, current.getForwardTrace(), current
                .getBackTrace(), metricData));
        if (depth == 0 && perEventCounters.containsKey(topLevelEvent)) {
            // We're at the top level, increment our event counters too
            EventCounters counters = perEventCounters.get(topLevelEvent);
            for (MetricMeasurement metric : current.getMetricInstances()) {
                EventMetricCounters counter = counters.getCounterForMetric(metric
                        .getMetricSource());
                if (counter != null) {
                    // We have potential race conditions here in that some metrics (e.g.
                    // SYSTEM_CPU_TIME) cannot be calculated atomically, as they are derived from 2
                    // non-atomic measurements. Depending on when the kernel counter increments, we
                    // may see spurious negative values on such derived metrics. We clip to 0 to
                    // avoid this. Example: at the start of a thread execution, we may get total 
                    // cpu=1000ms, user=800ms, system is calculated as 200ms. At completion, 
                    // total=1000ms, user=810ms (only user has 'ticked' over), system calculated as 
                    // 190ms. We spuriously think that the individual request has taken 
                    // (190 - 200) = -10ms.
                    counter.incrementCounters(Math.max(metric.totalValue(), 0L));
                }
            }
            counters.getInvocationCounter().incrementCounters(1);
        }
        current = current.getParent();
        if (depth == 0) {
            top = null;
        }
    }

    public void pauseForForward() {
        current.pauseAll();
    }

    public void resumeAfterForward() {
        current.resumeAll();
    }

    final StepMeasurements getInProgressMeasurements() {
        return top;
    }
}