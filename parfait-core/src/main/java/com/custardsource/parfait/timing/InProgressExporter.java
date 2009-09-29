package com.custardsource.parfait.timing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

public class InProgressExporter {
    private final EventTimer timer;
    private final ThreadContext context;
    
    public InProgressExporter(EventTimer timer, ThreadContext context) {
        this.timer = timer;
        this.context = context;
    }
    
    public TabularData captureInProgressMeasurements() throws OpenDataException {
        ThreadMetricSuite suite = timer.getMetricSuite();
        Map<Thread, EventMetricCollector> collectors = timer.getCollectorThreadMap();
        List<String> names = new ArrayList<String>();
        List<String> descriptions = new ArrayList<String>();
        List<OpenType<?>> types = new ArrayList<OpenType<?>>();
        
        names.add("Thread name");
        descriptions.add("Thread name");
        types.add(SimpleType.STRING);

        names.add("Event");
        descriptions.add("Event");
        types.add(SimpleType.STRING);

        for (ThreadMetric metric : suite.metrics()) {
            names.add(metric.getMetricName());
            descriptions.add(metric.getDescription());
            types.add(SimpleType.LONG);
        }

        
        Collection<String> contextKeys = (context == null) ? Collections.<String> emptyList()
                : context.allKeys();
        
        for (String contextEntry : contextKeys) {
            names.add(contextEntry);
            descriptions.add(contextEntry);
            types.add(SimpleType.STRING);
        }

        CompositeType rowType = new CompositeType("Snapshot row", "Snapshot row", names
                .toArray(new String[] {}), descriptions.toArray(new String[] {}), types
                .toArray(new OpenType<?>[] {}));

        TabularType type = new TabularType("Snapshot", "Snapshot", rowType,
                new String[] { "Thread name" });
        TabularData data = new TabularDataSupport(type);
        
        for (Map.Entry<Thread, EventMetricCollector> entry : collectors.entrySet()) {
            StepMeasurements m = entry.getValue().getInProgressMeasurements();
            String event = m.getBackTrace();
            Map<ThreadMetric, Long> snapshotValues = m.snapshotValues();
            Map<String, Object> keyedValues = new HashMap<String, Object>();
            keyedValues.put("Thread name", entry.getKey().getName());
            keyedValues.put("Event", event);
            for (ThreadMetric metric : suite.metrics()) {
                keyedValues.put(metric.getMetricName(), snapshotValues.get(metric));
                System.out.println(String.format("Thread %s in event %s, metric %s has value %s", entry.getKey(), event, metric, snapshotValues.get(metric)));
            }
            for (String contextEntry : contextKeys) {
                keyedValues.put(contextEntry, context.getForThread(entry.getKey(), contextEntry));
            }
            CompositeData row = new CompositeDataSupport(rowType, keyedValues);
            data.put(row);
        }
        return data;
    }

}
