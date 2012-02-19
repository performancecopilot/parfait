package com.custardsource.parfait.benchmark;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

class CPUThreadTestRunner implements Runnable {
    private final int iterations;
    private final boolean cpuTracingEnabled;
    private final CpuLookupMethod cpuLookupMethod;
    private BlockedMetricCollector blockedMetricCollector;

    static enum CpuLookupMethod {
        USE_CURRENT_THREAD_CPU_TIME, USE_CURRENT_THREAD_ID, USE_THREAD_INFO
    }
    
    public CPUThreadTestRunner(int iterations, boolean cpuTracingEnabled, CpuLookupMethod cpuLookupMethod) {
        this.iterations = iterations;
        this.cpuTracingEnabled = cpuTracingEnabled;
        this.cpuLookupMethod = cpuLookupMethod;
    }

    @Override
    public void run() {
        this.blockedMetricCollector = new BlockedMetricCollector();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        threadBean.setThreadContentionMonitoringEnabled(cpuTracingEnabled);

        for (int i = 0; i < iterations; i++) {
            switch (cpuLookupMethod) {
                case USE_CURRENT_THREAD_ID:
                    ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId());
                    break;
                case USE_CURRENT_THREAD_CPU_TIME:
                    ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
                    break;
                case USE_THREAD_INFO:
                    ThreadInfo threadInfo = ManagementFactory.getThreadMXBean().getThreadInfo(Thread.currentThread().getId());
                    ManagementFactory.getThreadMXBean().getThreadCpuTime(threadInfo.getThreadId());
                    break;
                default:
                    throw new IllegalStateException("Non-valid CpuLookupMethod: " + cpuLookupMethod);

            }
        }
        blockedMetricCollector.computeFinalValues();
    }

    public BlockedMetricCollector getBlockedMetricCollector() {
        return blockedMetricCollector;
    }
}