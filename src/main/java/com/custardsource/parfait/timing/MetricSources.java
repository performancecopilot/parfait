package com.custardsource.parfait.timing;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;

public class MetricSources {

    public static final MetricSource CLOCK_TIME_METRIC_SOURCE = new AbstractMetricSource(
            "Elapsed time", "ms") {
        public long getCurrentValue() {
            return System.currentTimeMillis();
        }
    };
    public static final MetricSource TOTAL_CPU_TIME_METRIC_SOURCE = new AbstractMetricSource(
            "Total CPU", "ms") {
        public long getCurrentValue() {
            return nanosToMillis(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime());
        }
    };
    public static final MetricSource USER_CPU_TIME_METRIC_SOURCE = new AbstractMetricSource(
            "User CPU", "ms") {
        public long getCurrentValue() {
            return nanosToMillis(ManagementFactory.getThreadMXBean().getCurrentThreadUserTime());
        }
    };

    public static final MetricSource SYSTEM_CPU_TIME_METRIC_SOURCE = new AbstractMetricSource(
            "System CPU", "ms") {
        public long getCurrentValue() {
            return nanosToMillis(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime()
                    - ManagementFactory.getThreadMXBean().getCurrentThreadUserTime());
        }
    };

    public static final MetricSource BLOCKED_COUNT_METRIC_SOURCE = new AbstractMetricSource(
            "Blocked count", "") {
        public long getCurrentValue() {
            return getCurrentThreadInfo().getBlockedCount();
        }
    };

    public static final MetricSource BLOCKED_TIME_METRIC_SOURCE = new AbstractMetricSource(
            "Blocked time", "ms") {
        public long getCurrentValue() {
            return getCurrentThreadInfo().getBlockedTime();
        }
    };

    public static final MetricSource WAITED_COUNT_METRIC_SOURCE = new AbstractMetricSource(
            "Wait count", "") {
        public long getCurrentValue() {
            return getCurrentThreadInfo().getWaitedCount();
        }
    };

    public static final MetricSource WAITED_TIME_METRIC_SOURCE = new AbstractMetricSource(
            "Wait time", "ms") {
        public long getCurrentValue() {
            return getCurrentThreadInfo().getWaitedTime();
        }
    };
    
    // TODO -- LoggingDriver, ErrorMessageMonitor

    private static ThreadInfo getCurrentThreadInfo() {
        return ManagementFactory.getThreadMXBean().getThreadInfo(Thread.currentThread().getId());
    }

    private static long nanosToMillis(long nanos) {
        final long NANOS_PER_MILLI = 1000000L;
        return nanos / NANOS_PER_MILLI;
    }
}
