package com.custardsource.parfait.benchmark;

import static com.custardsource.parfait.benchmark.BlockedMetricHelper.computeTotalBlockedCount;
import static com.custardsource.parfait.benchmark.BlockedMetricHelper.computeTotalBlockedTime;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.System.currentTimeMillis;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

public class CPUThreadTest {

    private static final int DEFAULT_THREAD_COUNT = 50;
    private static final int DEFAULT_ITERATIONS = 100000;

    @Parameter(names = {"-numThreads"}, required = false)
    private int numThreads = DEFAULT_THREAD_COUNT;

    @Parameter(names = {"-iterations"}, required = false)
    private int iterations = DEFAULT_ITERATIONS;

    public CPUThreadTest() {
    }

    private void doTest() {
        setUp();
        runBenchmark(true, CPUThreadTestRunner.CpuLookupMethod.USE_CURRENT_THREAD_CPU_TIME);
        runBenchmark(true, CPUThreadTestRunner.CpuLookupMethod.USE_CURRENT_THREAD_ID);
        runBenchmark(true, CPUThreadTestRunner.CpuLookupMethod.USE_THREAD_INFO);
        runBenchmark(false, CPUThreadTestRunner.CpuLookupMethod.USE_CURRENT_THREAD_CPU_TIME);
        runBenchmark(false, CPUThreadTestRunner.CpuLookupMethod.USE_CURRENT_THREAD_ID);
        runBenchmark(false, CPUThreadTestRunner.CpuLookupMethod.USE_THREAD_INFO);

    }

    private void runBenchmark(boolean cpuTracingEnabled, CPUThreadTestRunner.CpuLookupMethod cpuLookupMethod) {
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<CPUThreadTestRunner> executions = newArrayList();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        threadBean.setThreadCpuTimeEnabled(cpuTracingEnabled);

        threadBean.setThreadContentionMonitoringEnabled(true);

        long begin = currentTimeMillis();

        for (int i = 0; i < numThreads; i++) {
            CPUThreadTestRunner cpuThreadTestRunner = new CPUThreadTestRunner(iterations, cpuLookupMethod);
            executorService.execute(cpuThreadTestRunner);
            executions.add(cpuThreadTestRunner);
        }

        awaitExecutionCompletion(executorService);

        long end = currentTimeMillis();
        long timeTakenms = end - begin;
                
        report(executions,timeTakenms, iterations, cpuTracingEnabled, cpuLookupMethod);
    }

    private void report(List<CPUThreadTestRunner> cpuThreadTestRunners, double timeTakenms, int iterations, boolean cpuTracingEnabled, CPUThreadTestRunner.CpuLookupMethod cpuLookupMethod) {
        long totalBlockedCount = computeTotalBlockedCount(transformToListOfBlockedMetricCollectors(cpuThreadTestRunners));
        long totalBlockedTime = computeTotalBlockedTime(transformToListOfBlockedMetricCollectors(cpuThreadTestRunners));

        double iterationsPerSecond = iterations/(timeTakenms/1000);
        NumberFormat numberInstance = NumberFormat.getNumberInstance();
        String iterationsPerSecondString = numberInstance.format(iterationsPerSecond);

        System.out.printf("cpuTracingEnabled: %s\tcpuLookupMethod: %s\titerations/sec: %s\tblockedCount: %d\tblockedTime: %d\n", leftPadBoolean(cpuTracingEnabled), formatCpuLookupMethod(cpuLookupMethod), iterationsPerSecondString, totalBlockedCount, totalBlockedTime);
    }

    private static String formatCpuLookupMethod(CPUThreadTestRunner.CpuLookupMethod cpuLookupMethod) {
        return StringUtils.rightPad(cpuLookupMethod.name(), CPUThreadTestRunner.CpuLookupMethod.USE_CURRENT_THREAD_CPU_TIME.name().length());
    }

    private static String leftPadBoolean(boolean theBoolean) {
        return StringUtils.leftPad(Boolean.toString(theBoolean), 5);
    }

    private static void awaitExecutionCompletion(ExecutorService executorService) {
        try {
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<BlockedMetricCollector> transformToListOfBlockedMetricCollectors(List<CPUThreadTestRunner> cpuThreadTestRunners) {
        return Lists.transform(cpuThreadTestRunners, new Function<CPUThreadTestRunner, BlockedMetricCollector>() {
            @Override
            public BlockedMetricCollector apply(CPUThreadTestRunner input) {
                return input.getBlockedMetricCollector();
            }
        });
    }

    private void setUp() {
        ReportHelper.environmentReportHeader();
        System.out.printf("iterations: %d\n", iterations);
        System.out.printf("numThreads: %d\n", numThreads);
    }

    public static void main(String[] args) {

        CPUThreadTest cpuThreadTest = new CPUThreadTest();
        try {
            new JCommander(cpuThreadTest, args);
            cpuThreadTest.doTest();
        } catch (Exception e) {
            e.printStackTrace();
            new JCommander(cpuThreadTest).usage();
        }


    }
}
