package com.custardsource.parfait.benchmark;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

class CPUThreadTestRunner implements Runnable {
    private final int iterations;
    private final boolean useThreadIdLookup;
    private final boolean cpuTracingEnabled;
    private BlockedMetricCollector blockedMetricCollector;

    public CPUThreadTestRunner(int iterations, boolean cpuTracingEnabled, boolean useThreadIdLookup) {
        this.iterations = iterations;
        this.cpuTracingEnabled = cpuTracingEnabled;
        this.useThreadIdLookup = useThreadIdLookup;
    }

    //Commenting out different parts of the code produce different results. This will highlight the ThreadMXBean issues.
    //Also, while this program is running, start a shell script(a basic empty infinite for loop). The CPU usage will be close to what
    //we see in production.
    @Override
    public void run() {
        this.blockedMetricCollector = new BlockedMetricCollector();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        threadBean.setThreadContentionMonitoringEnabled(cpuTracingEnabled);

        for (int i = 0; i < iterations; i++) {
            if (useThreadIdLookup) {
                ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId());
            } else {
                ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
            }
        }
        blockedMetricCollector.computeFinalValues();
    }

    public BlockedMetricCollector getBlockedMetricCollector() {
        return blockedMetricCollector;
    }
}