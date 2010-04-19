package com.custardsource.parfait.timing;

import com.custardsource.parfait.*;

import javax.measure.unit.Unit;

public class CounterPairFactory {
    private final MonitorableRegistry registry;
    private final ThreadMetricSuite metricSuite;

    public CounterPairFactory(MonitorableRegistry registry, ThreadMetricSuite metricSuite) {
        this.registry = registry;
        this.metricSuite = metricSuite;
    }

    public Counter createCounterPair(Unit<?> unit, String globalCounterName, String threadMetricName,
            String threadMetricSuffix, String description) {
        MonitoredCounter metric = new MonitoredCounter(globalCounterName, description, registry, unit);
        ThreadCounter threadCounter = new ThreadCounter.ThreadMapCounter();

        ThreadMetric threadMetric = new ThreadValueMetric(threadMetricName, unit, threadMetricSuffix,
                description, threadCounter);
        metricSuite.addMetric(threadMetric);
        return new CounterPair(metric, threadCounter);
    }

}
