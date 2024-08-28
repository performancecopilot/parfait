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

package io.pcp.parfait.benchmark;

import static io.pcp.parfait.benchmark.BlockedMetricHelper.computeTotalBlockedCount;
import static io.pcp.parfait.benchmark.BlockedMetricHelper.computeTotalBlockedTime;
import static com.google.common.collect.Lists.newArrayList;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.pcp.parfait.DynamicMonitoringView;
import io.pcp.parfait.MonitorableRegistry;
import io.pcp.parfait.MonitoredCounter;
import io.pcp.parfait.dxm.IdentifierSourceSet;
import io.pcp.parfait.dxm.PcpMmvWriter;
import io.pcp.parfait.pcp.EmptyTextSource;
import io.pcp.parfait.pcp.MetricDescriptionTextSource;
import io.pcp.parfait.pcp.MetricNameMapper;
import io.pcp.parfait.pcp.PcpMonitorBridge;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

public class StandardMetricThroughPutBenchmark {

    private static final int CLUSTER_IDENTIFIER = 123;

    private final boolean startPcp ;
    private final boolean usePerMetricLock;
    private final int numThreads;
    private final int iterations;
    private final int numCounters;
    private final ExecutorService executorService;

    public StandardMetricThroughPutBenchmark(int numThreads, int numCounters, int iterations, boolean startPcp, boolean usePerMetricLock) {
        this.numThreads = numThreads;
        this.numCounters = numCounters;
        this.iterations = iterations;
        this.startPcp = startPcp;
        this.usePerMetricLock = usePerMetricLock;
        this.executorService = Executors.newFixedThreadPool(this.numThreads);
    }


    private void runBenchmark() throws InterruptedException {
        long begin = System.currentTimeMillis();
        MonitorableRegistry monitorableRegistry = new MonitorableRegistry();

        List<MonitoredCounter> counters = createCounters(numCounters, monitorableRegistry);


        final PcpMmvWriter mmvWriter = new PcpMmvWriter("parfait-microbenchmark-" + StandardMetricThroughPutBenchmark.class.getSimpleName() + ".mmv", IdentifierSourceSet.DEFAULT_SET);
        mmvWriter.setPerMetricLock(usePerMetricLock);

        mmvWriter.setClusterIdentifier(CLUSTER_IDENTIFIER);

        final PcpMonitorBridge pcpMonitorBridge = new PcpMonitorBridge(mmvWriter, MetricNameMapper.PASSTHROUGH_MAPPER, new MetricDescriptionTextSource(), new EmptyTextSource());

        DynamicMonitoringView selfStartingMonitoringView = new DynamicMonitoringView(monitorableRegistry, pcpMonitorBridge, 2000);

        if (startPcp) {
            selfStartingMonitoringView.start();
        }

        List<CounterIncrementer> counterIncrementers = executeBenchmark(numThreads, iterations, counters);
        if (startPcp) {
            selfStartingMonitoringView.stop();
        }

        long timeTaken = System.currentTimeMillis() - begin;

        report(startPcp, counters, timeTaken, counterIncrementers);
    }

    private List<MonitoredCounter> createCounters(int numCounters, MonitorableRegistry registry) {
        List<MonitoredCounter> counters = newArrayList();

        for (int i = 0; i < numCounters; i++) {
            counters.add(new MonitoredCounter("counter." + i, "Counter " + i, registry));
        }

        return counters;
    }

    private List<BlockedMetricCollector> transformToListOfBlockedMetricCollectors(List<CounterIncrementer> counterIncrementers) {
        return Lists.transform(counterIncrementers, new Function<CounterIncrementer, BlockedMetricCollector>() {
            @Override
            public BlockedMetricCollector apply(CounterIncrementer input) {
                return input.getBlockedMetricCollector();
            }
        });
    }
    private void report(boolean startPcp, List<MonitoredCounter> counters, long timeTaken, List<CounterIncrementer> counterIncrementers) {
        long totalBlockedCount = computeTotalBlockedCount(transformToListOfBlockedMetricCollectors(counterIncrementers));
        long totalBlockedTime = computeTotalBlockedTime(transformToListOfBlockedMetricCollectors(counterIncrementers));
        double counterIncrements = computeTotalCounterIncrements(counters);

        double incrementRate = counterIncrements / ((double) timeTaken / 1000);
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setGroupingUsed(true);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        String incrementRateString = StringUtils.leftPad(numberFormat.format(incrementRate), 15);

        System.out.printf("pcpStarted: %s\tperMetricLock: %s\tincrementRate(/sec): %s\t blockedCount: %d\t blockedTime: %d\n", startPcp, usePerMetricLock, incrementRateString, totalBlockedCount, totalBlockedTime);
    }

    private double computeTotalCounterIncrements(List<MonitoredCounter> counters) {
        double counterIncrements = 0;
        for (MonitoredCounter counter : counters) {
            counterIncrements += counter.get();
        }
        return counterIncrements;
    }


    private List<CounterIncrementer> executeBenchmark(int numThreads, int iterations, List<MonitoredCounter> counters) throws InterruptedException {
        List<CounterIncrementer> counterIncrementers = newArrayList();
        for (int i = 0; i < numThreads; i++) {
            CounterIncrementer counterIncrementer = new CounterIncrementer(counters, iterations);
            counterIncrementers.add(counterIncrementer);
            executorService.execute(counterIncrementer);
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        return counterIncrementers;
    }

    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        int numThreads = args.length<1?Runtime.getRuntime().availableProcessors():Integer.valueOf(args[0]);
        int numCounters = 1000;
        int iterations = 10000;

        ReportHelper.environmentReportHeader();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        System.out.printf("Thread Contention Supported: %s, Enabled by default: %s\n", threadMXBean.isThreadContentionMonitoringSupported(), threadMXBean.isThreadContentionMonitoringEnabled());
        System.out.printf("numThreads: %d, numCounters=%d, iterations=%d\n", numThreads, numCounters, iterations);

        new StandardMetricThroughPutBenchmark(numThreads, numCounters, iterations, false, false).runBenchmark();
        new StandardMetricThroughPutBenchmark(numThreads, numCounters, iterations, true, false).runBenchmark();
        new StandardMetricThroughPutBenchmark(numThreads, numCounters, iterations, true, true).runBenchmark();

        // now do it all again reverse, in case there's any JVM warmup issues unfairly treating the first/last runs
        new StandardMetricThroughPutBenchmark(numThreads, numCounters, iterations, true, true).runBenchmark();
        new StandardMetricThroughPutBenchmark(numThreads, numCounters, iterations, true, false).runBenchmark();
        new StandardMetricThroughPutBenchmark(numThreads, numCounters, iterations, false, false).runBenchmark();
    }

}
