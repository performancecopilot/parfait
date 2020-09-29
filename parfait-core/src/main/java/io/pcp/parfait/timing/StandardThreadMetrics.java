/*
 * Copyright 2009-2017 Aconex
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.pcp.parfait.timing;

import static systems.uom.unicode.CLDR.BYTE;
import static javax.measure.MetricPrefix.MILLI;
import static javax.measure.MetricPrefix.NANO;
import static tech.units.indriya.unit.Units.SECOND;
import static tech.units.indriya.AbstractUnit.ONE;

import com.google.common.collect.ImmutableList;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Collection;

import javax.measure.quantity.Time;
import javax.measure.Unit;

public class StandardThreadMetrics {
    private static final Unit<Time> MILLISECONDS = MILLI(SECOND);
    private static final Unit<Time> NANOSECONDS = NANO(SECOND);

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

    public static final ThreadMetric HEAP_BYTES = new AbstractThreadMetric("Heap Bytes", BYTE,
            "heap", "Amount of Heap (in bytes) used during the event") {
        @SuppressWarnings("restriction")
		@Override
        public long getValueForThread(Thread t) {
            java.lang.management.ThreadMXBean javaLangThreadMXBean = ManagementFactory.getThreadMXBean();
            if (javaLangThreadMXBean instanceof com.sun.management.ThreadMXBean) {
                com.sun.management.ThreadMXBean sunThreadMXBean = (com.sun.management.ThreadMXBean) javaLangThreadMXBean;
                return sunThreadMXBean.getThreadAllocatedBytes(t.getId());
            }
            return 0L;
        }
    };

    public static final ThreadMetric BLOCKED_COUNT = new ThreadInfoMetric("Blocked count", ONE,
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

    public static final ThreadMetric WAITED_COUNT = new ThreadInfoMetric("Wait count", ONE,
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
        return ImmutableList.of(CLOCK_TIME, TOTAL_CPU_TIME, USER_CPU_TIME, SYSTEM_CPU_TIME, HEAP_BYTES,
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
