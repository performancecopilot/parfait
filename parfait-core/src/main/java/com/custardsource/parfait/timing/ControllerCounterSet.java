package com.custardsource.parfait.timing;

import org.springframework.util.Assert;

import com.custardsource.parfait.MonitoredCounter;

/**
 * This class is a wrapper class which holds both a counter for a controller metric and another
 * counter for the same metric but its value is a total value across all controllers. It is
 * important to ensure that the total counter is the same instance of the class across all
 * ControllerCounterSet objects which are measuring the same metric.
 */
public class ControllerCounterSet {
    private final MonitoredCounter controllerCounter;
    private final MonitoredCounter totalCounter;

    public ControllerCounterSet(MonitoredCounter metricCounter, MonitoredCounter totalMetricCounter) {
        Assert.notNull(metricCounter, "Cannot provide null controller metric counter");
        Assert.notNull(totalMetricCounter, "Cannot provide null controller total metric counter");
        this.controllerCounter = metricCounter;
        this.totalCounter = totalMetricCounter;
    }

    public void incrementCounters(long value) {
        controllerCounter.inc(value);
        totalCounter.inc(value);

    }

    MonitoredCounter getTotalCounter() {
        return totalCounter;
    }

}
