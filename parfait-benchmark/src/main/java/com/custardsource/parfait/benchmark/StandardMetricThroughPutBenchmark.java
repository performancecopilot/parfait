package com.custardsource.parfait.benchmark;

import static com.google.common.collect.Lists.newArrayList;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.text.NumberFormat;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

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

        SelfStartingMonitoringView selfStartingMonitoringView = new SelfStartingMonitoringView(monitorableRegistry, pcpMonitorBridge, 2000);

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

    private void report(boolean startPcp, List<MonitoredCounter> counters, long timeTaken, List<CounterIncrementer> counterIncrementers) {
        long totalBlockedCount = computeTotalBlockedCount(counterIncrementers);
        long totalBlockedTime = computeTotalBlockedTime(counterIncrementers);
        double counterIncrements = computeTotalCounterIncrements(counters);
        
        double incrementRate = counterIncrements / ((double) timeTaken / 1000);
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setGroupingUsed(true);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        String incrementRateString = StringUtils.leftPad(numberFormat.format(incrementRate), 15);
        
        System.out.printf("pcpStarted: %s\tperMetricLock: %s\tincrementRate(/sec): %s\t blockedCount: %d\t blockedTime: %d\n", startPcp, usePerMetricLock, incrementRateString, totalBlockedCount, totalBlockedTime);
    }

    private long computeTotalBlockedCount(List<CounterIncrementer> counterIncrementers) {
        long totalBlockedCount = 0;
        for (CounterIncrementer counterIncrementer : counterIncrementers) {
            totalBlockedCount += counterIncrementer.getTotalBlockedCount();
        }
        return totalBlockedCount;
    }

    private long computeTotalBlockedTime(List<CounterIncrementer> counterIncrementers) {
        long totalBlockedTime = 0;
        for (CounterIncrementer counterIncrementer : counterIncrementers) {
            totalBlockedTime += counterIncrementer.getTotalBlockedTime();
        }
        return totalBlockedTime;
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

        System.out.printf("Host: %s\tJava: %s\n", Inet4Address.getLocalHost().getCanonicalHostName(), SystemUtils.JAVA_VERSION);
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
