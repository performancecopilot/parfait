package com.custardsource.parfait.timing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class InProgressSnapshot {
    private static final Logger LOG = Logger.getLogger(InProgressSnapshot.class);

    private final List<String> names = new ArrayList<String>();
    private final List<String> descriptions = new ArrayList<String>();
    private final List<Class<?>> classes = new ArrayList<Class<?>>();
    private final List<Map<String, Object>> values = Lists.newArrayList();

    private InProgressSnapshot(EventTimer timer, ThreadContext context) {
        names.add("Thread name");
        descriptions.add("Thread name");
        classes.add(String.class);

        names.add("Event");
        descriptions.add("Event");
        classes.add(String.class);

        for (ThreadMetric metric : timer.getMetricSuite().metrics()) {
            names.add(metric.getMetricName());
            descriptions.add(metric.getDescription());
            classes.add(Long.class);
        }

        Collection<String> contextKeys = (context == null) ? Collections.<String> emptyList()
                : context.allKeys();

        for (String contextEntry : contextKeys) {
            names.add(contextEntry);
            descriptions.add(contextEntry);
            classes.add(String.class);
        }

        for (Map.Entry<Thread, EventMetricCollector> entry : timer.getCollectorThreadMap()
                .entrySet()) {
            StepMeasurements m = entry.getValue().getInProgressMeasurements();
            if (m != null) {
                String event = m.getBackTrace();
                Map<ThreadMetric, Long> snapshotValues = m.snapshotValues();
                Map<String, Object> keyedValues = new HashMap<String, Object>();
                keyedValues.put("Thread name", entry.getKey().getName());
                keyedValues.put("Event", event);
                for (ThreadMetric metric : timer.getMetricSuite().metrics()) {
                    keyedValues.put(metric.getMetricName(), snapshotValues.get(metric));
                    LOG.trace(String.format("Thread %s in event %s, metric %s has value %s", entry
                            .getKey(), event, metric, snapshotValues.get(metric)));
                }
                for (String contextEntry : contextKeys) {
                    keyedValues.put(contextEntry, String.valueOf(context.getForThread(entry
                            .getKey(), contextEntry)));
                }
                values.add(keyedValues);
            }
        }

    }

    public int getColumnCount() {
        return names.size();
    }

    public List<Map<String, Object>> getValues() {
        return ImmutableList.copyOf(values);
    }

    public Collection<String> getColumnDescriptions() {
        return ImmutableList.copyOf(descriptions);
    }

    public Collection<String> getColumnNames() {
        return ImmutableList.copyOf(names);
    }

    public List<Class<?>> getColumnClasses() {
        return ImmutableList.copyOf(classes);
    }

    public static InProgressSnapshot capture(EventTimer timer, ThreadContext context) {
        return new InProgressSnapshot(timer, context);
    }

    public String asTabbedString() {
        return TO_TABBED_STRING.apply(this);
    }

    public String asFormattedString() {
        return TO_FORMATTED_STRING.apply(this);
    }

    private static class InProgressFormatter implements Function<InProgressSnapshot, String> {
        @Override
        public String apply(InProgressSnapshot from) {
            StringBuilder result = new StringBuilder();

            for (String column : from.getColumnNames()) {
                result.append(formatColumnValue(column)).append("\t");
            }
            result.append("\n");
            for (Map<String, Object> rowData : from.getValues()) {
                for (String column : from.getColumnNames()) {
                    result.append(formatColumnValue(String.valueOf(rowData.get(column)))).append(
                            "\t");
                }
                result.append("\n");

            }
            return result.toString();
        }

        protected String formatColumnValue(String value) {
            return value;
        }
    }

    public static final Function<InProgressSnapshot, String> TO_TABBED_STRING = new InProgressFormatter();

    public static final Function<InProgressSnapshot, String> TO_FORMATTED_STRING = new InProgressFormatter() {
        @Override
        protected String formatColumnValue(String value) {
            return StringUtils.leftPad(value, 20);
        }
    };
}