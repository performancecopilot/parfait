package com.custardsource.parfait.benchmark;

import java.lang.management.ManagementFactory;
import java.util.List;

import com.custardsource.parfait.MonitoredCounter;

class CounterIncrementer implements Runnable{

    private final List<MonitoredCounter> counters;
    private final int iterations;
    private BlockedMetricCollector blockedMetricCollector;

    public CounterIncrementer(List<MonitoredCounter> counters, int iterations) {
        this.counters = counters;
        this.iterations = iterations;
    }

    @Override
    public void run() {
        ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(true);

        this.blockedMetricCollector = new BlockedMetricCollector();
        for (int i = 0; i < iterations; i++) {

            for (MonitoredCounter counter : counters) {
                counter.inc();
            }
        }
        blockedMetricCollector.computeFinalValues();
    }

    public BlockedMetricCollector getBlockedMetricCollector() {
        return blockedMetricCollector;
    }
}
