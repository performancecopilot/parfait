package com.custardsource.parfait.timing;

import java.util.Map;

import org.apache.log4j.Logger;

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
    private Timeable topLevelEvent;

    private final Map<Timeable, EventCounters> perEventCounters;

    private static final Logger LOG = Logger.getLogger(EventMetricCollector.class);

    public EventMetricCollector(Map<Timeable, EventCounters> perEventCounters) {
        this.perEventCounters = perEventCounters;
    }

    public void startTiming(Timeable event, String action) {
        StepMeasurements newTiming = new StepMeasurements(current, event.getClass(),
                action);
        for (ThreadMetric metric : perEventCounters.get(event).getMetricSources()) {
            newTiming.addMetricInstance(new MetricMeasurement(metric, Thread.currentThread()));
        }
        current = newTiming;
        topLevelEvent = event;
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
                    counter.incrementCounters(metric.totalValue());
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