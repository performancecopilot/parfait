package com.custardsource.parfait.benchmark;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.List;

import com.custardsource.parfait.MonitoredCounter;

class CounterIncrementer implements Runnable{

    private final List<MonitoredCounter> counters;
    private final int iterations;
    private ThreadInfo initialThreadInfo;
    private long totalBlockedCount;
    private long totalBlockedTime;

    public CounterIncrementer(List<MonitoredCounter> counters, int iterations) {
        this.counters = counters;
        this.iterations = iterations;
    }

    @Override
    public void run() {

        ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(true);

        this.initialThreadInfo = ManagementFactory.getThreadMXBean().getThreadInfo(Thread.currentThread().getId());
        long initialBlockedCount = initialThreadInfo.getBlockedCount();
        long initialBlockedTime = initialThreadInfo.getBlockedTime();
        for (int i = 0; i < iterations; i++) {

            for (MonitoredCounter counter : counters) {
                counter.inc();
            }
        }

        ThreadInfo finalThreadInfo = ManagementFactory.getThreadMXBean().getThreadInfo(Thread.currentThread().getId());
        long finalBlockedCount = finalThreadInfo.getBlockedCount();
        long finalBlockedTime = finalThreadInfo.getBlockedTime();

        totalBlockedCount = finalBlockedCount-initialBlockedCount;
        totalBlockedTime = finalBlockedTime-initialBlockedTime;
    }


    public long getTotalBlockedCount() {
        return totalBlockedCount;
    }

    public long getTotalBlockedTime() {
        return totalBlockedTime;
    }
}
