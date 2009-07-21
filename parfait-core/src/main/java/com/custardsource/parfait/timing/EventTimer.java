package com.custardsource.parfait.timing;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.apache.log4j.Logger;

import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.MonitoredCounter;

/**
 * A class to provide a {@link EventMetricCollector} to each {@link Timeable} on demand, guaranteed
 * to be thread-safe as long is it's only ever used by the requesting thread.
 */
public class EventTimer {

    /**
     * Setting this {@link Logger} to DEBUG level will list all the created metrics in a
     * tab-delimited format, useful for importing elsewhere
     */
    private static final Logger LOG = Logger.getLogger(EventTimer.class);

    private final Map<Timeable, EventCounters> perTimeableCounters = new ConcurrentHashMap<Timeable, EventCounters>();

    private final ThreadValue<EventMetricCollector> metricCollectors = new ThreadValue.WeakReferenceThreadMap<EventMetricCollector>() {
        @Override
        protected EventMetricCollector initialValue() {
            return new EventMetricCollector(perTimeableCounters);
        }
    };
    
    
    /**
     * Holds the singleton total counters which are used across all events. The key is the
     * metric name
     */
    private final Map<String, MonitoredCounter> totalCountersAcrossEvents = new HashMap<String, MonitoredCounter>();
    
    private final ThreadMetricSuite metricSuite;
    private final String prefix;
    private final MonitorableRegistry registry;

    public EventTimer(String prefix, MonitorableRegistry registry, ThreadMetricSuite metrics,
            boolean enableCpuCollection, boolean enableContentionCollection) {
        this.metricSuite = metrics;
        this.prefix = prefix;
        this.registry = registry;
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
        timeable.setEventTimer(this);
        perTimeableCounters.put(timeable, getCounterSet(beanName));
    }

    private EventCounters getCounterSet(String beanName) {
        EventMetricCounters invocationCounter = createEventMetricCounters(beanName, "count",
                "Total number of times the event was directly triggered");
        EventCounters counters = new EventCounters(invocationCounter);

        for (ThreadMetric metric : metricSuite.metrics()) {
            EventMetricCounters timingCounter = createEventMetricCounters(beanName, metric
                    .getCounterSuffix(), metric.getDescription());
            counters.addMetric(metric, timingCounter);
        }

        return counters;
    }

    private MonitoredCounter createMetric(String beanName, String metric, String description) {
        String metricName = getMetricName(beanName, metric);
        String metricDescription = String.format(description, beanName);
        LOG.debug("Created metric: " + metricName + "\t" + metricDescription);
        return new MonitoredCounter(metricName, metricDescription, registry);
    }

    private EventMetricCounters createEventMetricCounters(String beanName, String metric,
            String metricDescription) {
        MonitoredCounter metricCounter = createMetric(beanName, metric, metricDescription + " ["
                + beanName + "]");
        MonitoredCounter totalCounter;

        totalCounter = totalCountersAcrossEvents.get(metric);
        if (totalCounter == null) {
            totalCounter = new MonitoredCounter(getTotalMetricName(metric), metricDescription
                    + " [TOTAL]", registry);
            totalCountersAcrossEvents.put(metric, totalCounter);
        }

        return new EventMetricCounters(metricCounter, totalCounter);
    }

    private String getMetricName(String beanName, String metric) {
        // TODO do name cleanup elsewhere
        return prefix + "." + beanName.replace("/", "") + "." + metric;
    }

    private String getTotalMetricName(String metric) {
        return prefix + ".total." + metric;
    }

    Integer getNumberOfTotalEventCounters() {
        return totalCountersAcrossEvents.size();
    }

    EventCounters getCounterSetForEvent(Object event) {
        return perTimeableCounters.get(event);
    }

    public TabularData captureInProgressMeasurements() throws OpenDataException {
        List<String> names = new ArrayList<String>();
        List<String> descriptions = new ArrayList<String>();
        List<OpenType<?>> types = new ArrayList<OpenType<?>>();
        
        names.add("Thread name");
        descriptions.add("Thread name");
        types.add(SimpleType.STRING);

        names.add("Event");
        descriptions.add("Event");
        types.add(SimpleType.STRING);

        for (ThreadMetric metric : metricSuite.metrics()) {
            names.add(metric.getMetricName());
            descriptions.add(metric.getDescription());
            types.add(SimpleType.LONG);
        }
        
        CompositeType rowType = new CompositeType("Snapshot row", "Snapshot row", names
                .toArray(new String[] {}), descriptions.toArray(new String[] {}), types
                .toArray(new OpenType<?>[] {}));

        TabularType type = new TabularType("Snapshot", "Snapshot", rowType,
                new String[] { "Thread name" });
        TabularData data = new TabularDataSupport(type);
        
        Map<Thread, EventMetricCollector> collectors = metricCollectors.asMap();
        for (Map.Entry<Thread, EventMetricCollector> entry : collectors.entrySet()) {
            StepMeasurements m = entry.getValue().getInProgressMeasurements();
            String event = m.getBackTrace();
            Map<ThreadMetric, Long> snapshotValues = m.snapshotValues();
            Map<String, Object> keyedValues = new HashMap<String, Object>();
            keyedValues.put("Thread name", entry.getKey().getName());
            keyedValues.put("Event", event);
            for (ThreadMetric metric : metricSuite.metrics()) {
                keyedValues.put(metric.getMetricName(), snapshotValues.get(metric));
                System.out.println(String.format("Thread %s in event %s, metric %s has value %s", entry.getKey(), event, metric, snapshotValues.get(metric)));
            }
            CompositeData row = new CompositeDataSupport(rowType, keyedValues);
            data.put(row);
        }
        return data;
    }
}
