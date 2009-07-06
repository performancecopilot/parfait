package com.custardsource.parfait.timing;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.custardsource.parfait.MonitoredCounter;

/**
 * A class to provide a {@link EventMetricCollector} to each controller on demand, guaranteed
 * to be thread-safe as long is it's only ever used by the requesting thread.
 */
public class EventTimer {

    /**
     * Setting this {@link Logger} to DEBUG level will list all the created PCP metrics in a
     * tab-delimited format, useful for adding to the agent.
     */
    private static final Logger LOG = Logger.getLogger(EventTimer.class);

    private final Map<Timeable, EventCounters> perControllerCounters = new ConcurrentHashMap<Timeable, EventCounters>();

    private final ThreadLocal<EventMetricCollector> metricCollectors = new ThreadLocal<EventMetricCollector>() {
        @Override
        protected EventMetricCollector initialValue() {
            return new EventMetricCollector(perControllerCounters);
        }
    };
    
    /**
     * Holds the singleton total counters which are used across all controllers. The key is the
     * metric name
     */
    private final Map<String, MonitoredCounter> totalCountersForControllers = new HashMap<String, MonitoredCounter>();

    private final ThreadMetricSuite metricSuite;

    protected EventTimer() {
        this(new ThreadMetricSuite());
    }
    
    protected EventTimer(ThreadMetricSuite metrics) {
        this.metricSuite = metrics;
        // used by subclasses
    }
    
    public EventTimer(boolean enableCpuCollection,
            boolean enableContentionCollection) {
        this(new ThreadMetricSuite(), enableCpuCollection, enableContentionCollection);
    }

    public EventTimer(ThreadMetricSuite metrics, boolean enableCpuCollection,
            boolean enableContentionCollection) {
        this.metricSuite = metrics;
        if (enableCpuCollection) {
            ManagementFactory.getThreadMXBean().setThreadCpuTimeEnabled(true);
        }
        if (enableContentionCollection) {
            ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(true);
        }
    }

    public EventMetricCollector getCollector() {
        return metricCollectors.get();
    }

    public void registerTimeable(Timeable timeable, String beanName) {
        timeable.setMetricCollectorFactory(this);
        perControllerCounters.put(timeable, getCounterSet(beanName));
    }

    private EventCounters getCounterSet(String beanName) {
        EventMetricCounters invocationCounter = createControllerMonitoredCounter(beanName, "count",
                "Total number of times the event was directly triggered");
        EventCounters counters = new EventCounters(invocationCounter);

        for (ThreadMetric metric : metricSuite.metrics()) {
            EventMetricCounters timingCounter = createControllerMonitoredCounter(beanName, metric
                    .getCounterSuffix(), metric.getDescription());
            counters.addMetric(metric, timingCounter);
        }

        return counters;
    }

    private MonitoredCounter createMetric(String beanName, String metric, String description) {
        String metricName = getMetricName(beanName, metric);
        String metricDescription = String.format(description, beanName);
        LOG.debug("Created metric: " + metricName + "\t" + metricDescription);
        return new MonitoredCounter(metricName, metricDescription);
    }

    private EventMetricCounters createControllerMonitoredCounter(String beanName, String metric,
            String metricDescription) {
        MonitoredCounter metricCounter = createMetric(beanName, metric, metricDescription + " ["
                + beanName + "]");
        MonitoredCounter totalCounter;

        totalCounter = totalCountersForControllers.get(metric);
        if (totalCounter == null) {
            totalCounter = new MonitoredCounter(getTotalMetricName(metric), metricDescription
                    + " [TOTAL]");
            totalCountersForControllers.put(metric, totalCounter);
        }

        return new EventMetricCounters(metricCounter, totalCounter);
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

    EventCounters getCounterSetForController(Object controller) {
        return perControllerCounters.get(controller);
    }

}
