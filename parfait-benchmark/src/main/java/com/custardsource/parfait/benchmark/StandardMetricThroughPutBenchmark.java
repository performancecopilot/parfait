package com.custardsource.parfait.benchmark;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.MonitoredCounter;
import com.custardsource.parfait.dxm.IdentifierSourceSet;
import com.custardsource.parfait.dxm.PcpMmvWriter;
import com.custardsource.parfait.pcp.EmptyTextSource;
import com.custardsource.parfait.pcp.MetricDescriptionTextSource;
import com.custardsource.parfait.pcp.MetricNameMapper;
import com.custardsource.parfait.pcp.PcpMonitorBridge;
import com.custardsource.parfait.spring.SelfStartingMonitoringView;

public class StandardMetricThroughPutBenchmark {


    private static final int CLUSTER_IDENTIFIER = 123;

    public static void main(String[] args) throws InterruptedException {

        int numThreads = 8;
        int iterations = 1000;
        int numCounters = 1000;

        long begin = System.currentTimeMillis();
        MonitorableRegistry monitorableRegistry = new MonitorableRegistry();

        List<MonitoredCounter> counters = createCounters(numCounters, monitorableRegistry);


        final PcpMmvWriter mmvWriter = new PcpMmvWriter("parfait-microbenchmark-" + StandardMetricThroughPutBenchmark.class.getSimpleName() + ".mmv", IdentifierSourceSet.DEFAULT_SET);
        mmvWriter.setClusterIdentifier(CLUSTER_IDENTIFIER);

        final PcpMonitorBridge pcpMonitorBridge = new PcpMonitorBridge(mmvWriter, MetricNameMapper.PASSTHROUGH_MAPPER, new MetricDescriptionTextSource(), new EmptyTextSource());

        SelfStartingMonitoringView selfStartingMonitoringView = new SelfStartingMonitoringView(monitorableRegistry, pcpMonitorBridge, 2000);
        selfStartingMonitoringView.start();

        List<CounterIncrementer> counterIncrementers = executeBenchmark(numThreads, iterations, counters);
        selfStartingMonitoringView.stop();

        long timeTaken = System.currentTimeMillis() - begin;

        report(numThreads, iterations, counters, timeTaken, counterIncrementers);
        

    }

    private static List<MonitoredCounter> createCounters(int numCounters, MonitorableRegistry registry) {
        List<MonitoredCounter> counters = newArrayList();

        for (int i = 0; i < numCounters; i++) {
            counters.add(new MonitoredCounter("counter." + i, "Counter " + i, registry));
        }
        
        return counters;
    }

    private static void report(int numThreads, int iterations, List<MonitoredCounter> counters, long timeTaken, List<CounterIncrementer> counterIncrementers) {
        long totalBlockedCount = computeTotalBlockedCount(counterIncrementers);
        long totalBlockedTime = computeTotalBlockedTime(counterIncrementers);
        double counterIncrements = computeTotalCounterIncrements(counters);
        double incrementRate = counterIncrements / ((double) timeTaken / 1000);

        System.out.printf("iterations: %d, numThreads: %d, numCounters: %d, incrementRate: %.2f/sec, blockedCount: %d, blockedTime: %d", iterations, numThreads, counters.size(), incrementRate, totalBlockedCount, totalBlockedTime);
    }

    private static long computeTotalBlockedCount(List<CounterIncrementer> counterIncrementers) {
        long totalBlockedCount = 0;
        for (CounterIncrementer counterIncrementer : counterIncrementers) {
            totalBlockedCount+=counterIncrementer.getTotalBlockedCount();
        }
        return totalBlockedCount;
    }
    private static long computeTotalBlockedTime(List<CounterIncrementer> counterIncrementers) {
        long totalBlockedTime = 0;
        for (CounterIncrementer counterIncrementer : counterIncrementers) {
            totalBlockedTime+=counterIncrementer.getTotalBlockedTime();
        }
        return totalBlockedTime;
    }

    private static double computeTotalCounterIncrements(List<MonitoredCounter> counters) {
        double counterIncrements = 0;
        for (MonitoredCounter counter : counters) {
            counterIncrements += counter.get();
        }
        return counterIncrements;
    }


    private static List<CounterIncrementer> executeBenchmark(int numThreads, int iterations, List<MonitoredCounter> counters) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

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

}
