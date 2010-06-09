package com.custardsource.parfait.timing;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Collection;

import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.google.common.collect.ImmutableList;

public class StandardThreadMetrics {
    private static final Unit<?> MILLISECONDS = SI.MILLI(SI.SECOND);
    private static final Unit<?> NANOSECONDS = SI.NANO(SI.SECOND);

    public static final ThreadMetric CLOCK_TIME = new AbstractThreadMetric("Elapsed time", NANOSECONDS,
            "time", "Total wall time (in ms) spent executing event") {
        @Override
        public long getValueForThread(Thread t) {
            return System.nanoTime();
        }
    };
    
    public static final ThreadMetric TOTAL_CPU_TIME = new AbstractThreadMetric("Total CPU", NANOSECONDS,
            "cputime", "Total CPU time (in ns) spent executing event") {
        @Override
        public long getValueForThread(Thread t) {
            return ManagementFactory.getThreadMXBean().getThreadCpuTime(t.getId());
        }
    };

    public static final ThreadMetric USER_CPU_TIME = new AbstractThreadMetric("User CPU", NANOSECONDS,
            "utime", "User CPU time (in ns) spent executing event") {
        @Override
        public long getValueForThread(Thread t) {
            return ManagementFactory.getThreadMXBean().getThreadUserTime(t.getId());
        }
    };

    public static final ThreadMetric SYSTEM_CPU_TIME = new AbstractThreadMetric("System CPU", NANOSECONDS,
            "stime", "System CPU time (in ns) spent executing event") {
        @Override
        public long getValueForThread(Thread t) {
            return ManagementFactory.getThreadMXBean().getThreadCpuTime(t.getId())
                    - ManagementFactory.getThreadMXBean().getThreadUserTime(t.getId());
        }
    };

    public static final ThreadMetric BLOCKED_COUNT = new ThreadInfoMetric("Blocked count", Unit.ONE,
            "blocked.count", "Number of times thread entered BLOCKED state during event") {
        @Override
        public long getValue(ThreadInfo threadInfo) {
            return threadInfo.getBlockedCount();
        }
    };

    public static final ThreadMetric BLOCKED_TIME = new ThreadInfoMetric("Blocked time", MILLISECONDS,
            "blocked.time", "ms spent in BLOCKED state during event") {
        @Override
        public long getValue(ThreadInfo threadInfo) {
            return threadInfo.getBlockedTime();
        }
    };

    public static final ThreadMetric WAITED_COUNT = new ThreadInfoMetric("Wait count", Unit.ONE,
            "waited.count",
            "Number of times thread entered WAITING or TIMED_WAITING state during event") {
        @Override
        public long getValue(ThreadInfo threadInfo) {
            return threadInfo.getWaitedCount();
        }
    };

    public static final ThreadMetric WAITED_TIME = new ThreadInfoMetric("Wait time", MILLISECONDS,
            "waited.time", "ms spent in WAITING or TIMED_WAITING state during event") {
        @Override
        public long getValue(ThreadInfo threadInfo) {
            return threadInfo.getWaitedTime();
        }
    };

    public static Collection<? extends ThreadMetric> defaults() {
        // TOTAL_CPU_TIME is not included by default (you can get it from other places)
        return ImmutableList.of(CLOCK_TIME, TOTAL_CPU_TIME, USER_CPU_TIME, SYSTEM_CPU_TIME,
                BLOCKED_COUNT, BLOCKED_TIME, WAITED_COUNT, WAITED_TIME);
    }

    private static abstract class ThreadInfoMetric extends AbstractThreadMetric {
        public ThreadInfoMetric(String name, Unit<?> unit, String counterSuffix, String description) {
            super(name, unit, counterSuffix, description);
        }

        @Override
        public final long getValueForThread(Thread t) {
            ThreadInfo info = getThreadInfo(t);
            return info == null ? 0 : getValue(info);
        }

        protected abstract long getValue(ThreadInfo threadInfo);

        private static ThreadInfo getThreadInfo(Thread t) {
            return ManagementFactory.getThreadMXBean().getThreadInfo(t.getId());
        }
    }
}
