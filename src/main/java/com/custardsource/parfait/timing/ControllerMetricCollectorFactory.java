package com.custardsource.parfait.timing;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.custardsource.parfait.MonitoredCounter;
import com.custardsource.parfait.timing.ControllerMetricCollector.MonitoredCounterSet;

/**
 * A class to provide a {@link ControllerMetricCollector} to each controller on demand, guaranteed
 * to be thread-safe as long is it's only ever used by the requesting thread.
 */
public class ControllerMetricCollectorFactory {

    /**
     * Setting this {@link Logger} to DEBUG level will list all the created PCP metrics in a
     * tab-delimited format, useful for adding to the agent.
     */
    private static final Logger LOG = Logger.getLogger(ControllerMetricCollectorFactory.class);

    private final Map<MetricCollectorController, ControllerMetricCollector.MonitoredCounterSet> perControllerCounters = new ConcurrentHashMap<MetricCollectorController, ControllerMetricCollector.MonitoredCounterSet>();

    private final ThreadLocal<ControllerMetricCollector> metricCollectors = new ThreadLocal<ControllerMetricCollector>() {
        @Override
        protected ControllerMetricCollector initialValue() {
            return new ControllerMetricCollector(perControllerCounters);
        }
    };
    /**
     * Holds the singleton total counters which are used across all controllers. The key is the
     * metric name
     */
    private Map<String, MonitoredCounter> totalCountersForControllers = new HashMap<String, MonitoredCounter>();

    protected ControllerMetricCollectorFactory() {
        // used by subclasses
    }

    public ControllerMetricCollectorFactory(boolean enableCpuCollection,
            boolean enableContentionCollection) {
        if (enableCpuCollection) {
            ManagementFactory.getThreadMXBean().setThreadCpuTimeEnabled(true);
        }
        if (enableContentionCollection) {
            ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(true);
        }
    }

    public ControllerMetricCollector getCollector() {
        return metricCollectors.get();
    }

    public void addController(MetricCollectorController controller, String beanName) {
        controller.setMetricCollectorFactory(this);
        perControllerCounters.put(controller, getCounterSet(beanName));
    }

    private ControllerMetricCollector.MonitoredCounterSet getCounterSet(String beanName) {
        ControllerCounterSet invocationCounter = createControllerMonitoredCounter(beanName,
                "count",
                "Total number of times the controller %s was invoked directly by a user request",
                "Total number of times all controllers have been invoked directly by a user request");
        ControllerMetricCollector.MonitoredCounterSet counters = new ControllerMetricCollector.MonitoredCounterSet(
                invocationCounter);

        ControllerCounterSet timingCounter = createControllerMonitoredCounter(
                beanName,
                "time",
                "Total time (in ms) spent in controller %s after direct invocation by a user request",
                "Total time (in ms) spent in all controllers after direct invocation by a user request");
        counters.addMetric(StandardThreadMetrics.CLOCK_TIME_METRIC_SOURCE, timingCounter);

        ControllerCounterSet userTimeCounter = createControllerMonitoredCounter(beanName, "utime",
                "User CPU time spent in controller %s after direct invocation",
                "User CPU time spent in all controllers after direct invocation");
        counters.addMetric(StandardThreadMetrics.USER_CPU_TIME_METRIC_SOURCE, userTimeCounter);

        ControllerCounterSet systemTimeCounter = createControllerMonitoredCounter(beanName,
                "stime", "System CPU time spent in controller %s after direct invocation",
                "System CPU time spent in all controllers after direct invocation");
        counters.addMetric(StandardThreadMetrics.SYSTEM_CPU_TIME_METRIC_SOURCE, systemTimeCounter);

        ControllerCounterSet blockedCountCounter = createControllerMonitoredCounter(beanName,
                "blocked.count",
                "Number of times thread entered BLOCKED state during controller %s",
                "Number of times BLOCKED state entered in all controllers");
        counters.addMetric(StandardThreadMetrics.BLOCKED_COUNT_METRIC_SOURCE, blockedCountCounter);

        ControllerCounterSet blockedTimeCounter = createControllerMonitoredCounter(beanName,
                "blocked.time", "ms spent in BLOCKED state during controller %s",
                "ms spent in BLOCKED state in all controllers");
        counters.addMetric(StandardThreadMetrics.BLOCKED_TIME_METRIC_SOURCE, blockedTimeCounter);

        ControllerCounterSet waitedCountCounter = createControllerMonitoredCounter(
                beanName,
                "waited.count",
                "Number of times thread entered WAITING or TIMED_WAITING state during controller %s",
                "Number of times WAITING or TIMED_WAITING state entered in all controllers");
        counters.addMetric(StandardThreadMetrics.WAITED_COUNT_METRIC_SOURCE, waitedCountCounter);

        ControllerCounterSet waitedTimeCounter = createControllerMonitoredCounter(beanName,
                "waited.time", "ms spent in WAITED or TIMED_WAITING state during controller %s",
                "ms spent in WAITED or TIMED_WAITING state in all controllers");
        counters.addMetric(StandardThreadMetrics.WAITED_TIME_METRIC_SOURCE, waitedTimeCounter);

        // TODO - db and error metrics
        return counters;
    }

    private MonitoredCounter createMetric(String beanName, String metric, String description) {
        String metricName = getMetricName(beanName, metric);
        String metricDescription = String.format(description, beanName);
        LOG.debug("Created metric: " + metricName + "\t" + metricDescription);
        return new MonitoredCounter(metricName, metricDescription);
    }

    private ControllerCounterSet createControllerMonitoredCounter(String beanName, String metric,
            String metricDescription, String totalMetricDescription) {
        MonitoredCounter metricCounter = createMetric(beanName, metric, metricDescription);
        MonitoredCounter totalCounter;

        totalCounter = totalCountersForControllers.get(metric);
        if (totalCounter == null) {
            totalCounter = new MonitoredCounter(getTotalMetricName(metric), totalMetricDescription);
            totalCountersForControllers.put(metric, totalCounter);
        }

        return new ControllerCounterSet(metricCounter, totalCounter);
    }

    private String getMetricName(String beanName, String metric) {
        return "aconex.controllers." + beanName.replace("/", "") + "." + metric;
    }

    private String getTotalMetricName(String metric) {
        return "aconex.controllers.total." + metric;
    }

    Integer getNumberOfTotalControllerCounters() {
        return totalCountersForControllers.size();
    }

    MonitoredCounterSet getCounterSetForController(Object controller) {
        return perControllerCounters.get(controller);
    }

}
