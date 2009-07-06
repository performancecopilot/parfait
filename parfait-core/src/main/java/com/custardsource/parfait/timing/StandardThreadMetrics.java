package com.custardsource.parfait.timing;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Collection;

import com.google.common.collect.ImmutableList;

public class StandardThreadMetrics {
    public static final ThreadMetric CLOCK_TIME = new AbstractThreadMetric("Elapsed time", "ms",
            "time", "Total wall time (in ms) spent executing event") {
        public long getCurrentValue() {
            return System.currentTimeMillis();
        }
    };
    public static final ThreadMetric TOTAL_CPU_TIME = new AbstractThreadMetric("Total CPU", "ms",
            "cputime", "Total CPU time (in ms) spent executing event") {
        public long getCurrentValue() {
            return nanosToMillis(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime());
        }
    };

    public static final ThreadMetric USER_CPU_TIME = new AbstractThreadMetric("User CPU", "ms",
            "utime", "User CPU time (in ms) spent executing event") {
        public long getCurrentValue() {
            return nanosToMillis(ManagementFactory.getThreadMXBean().getCurrentThreadUserTime());
        }
    };

    public static final ThreadMetric SYSTEM_CPU_TIME = new AbstractThreadMetric("System CPU", "ms",
            "stime", "System CPU time (in ms) spent executing event") {
        public long getCurrentValue() {
            return nanosToMillis(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime()
                    - ManagementFactory.getThreadMXBean().getCurrentThreadUserTime());
        }
    };

    public static final ThreadMetric BLOCKED_COUNT = new AbstractThreadMetric("Blocked count", "",
            "blocked.count", "Number of times thread entered BLOCKED state during event") {
        public long getCurrentValue() {
            return getCurrentThreadInfo().getBlockedCount();
        }
    };

    public static final ThreadMetric BLOCKED_TIME = new AbstractThreadMetric("Blocked time", "ms",
            "blocked.time", "ms spent in BLOCKED state during event") {
        public long getCurrentValue() {
            return getCurrentThreadInfo().getBlockedTime();
        }
    };

    public static final ThreadMetric WAITED_COUNT = new AbstractThreadMetric("Wait count", "",
            "waited.count", "Number of times thread entered WAITING or TIMED_WAITING state during event") {
        public long getCurrentValue() {
            return getCurrentThreadInfo().getWaitedCount();
        }
    };

    public static final ThreadMetric WAITED_TIME = new AbstractThreadMetric("Wait time", "ms",
            "waited.time", "ms spent in WAITING or TIMED_WAITING state during event") {
        public long getCurrentValue() {
            return getCurrentThreadInfo().getWaitedTime();
        }
    };

    private static ThreadInfo getCurrentThreadInfo() {
        return ManagementFactory.getThreadMXBean().getThreadInfo(Thread.currentThread().getId());
    }

    private static long nanosToMillis(long nanos) {
        final long NANOS_PER_MILLI = 1000000L;
        return nanos / NANOS_PER_MILLI;
    }

    public static Collection<? extends ThreadMetric> defaults() {
        // TOTAL_CPU_TIME is not included by default (you can get it from other places)
        return ImmutableList.of(CLOCK_TIME, TOTAL_CPU_TIME, USER_CPU_TIME, SYSTEM_CPU_TIME,
                BLOCKED_COUNT, BLOCKED_TIME, WAITED_COUNT, WAITED_TIME);
    }
}
