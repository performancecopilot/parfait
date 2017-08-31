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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class InProgressSnapshot {
    private static final Logger LOG = LoggerFactory.getLogger(InProgressSnapshot.class);
    public static final Function<InProgressSnapshot, String> TO_TABBED_STRING = new InProgressFormatter();
    public static final Function<InProgressSnapshot, String> TO_FORMATTED_STRING = new InProgressFormatter() {
        @Override
        protected String formatColumnValue(String value) {
            return Strings.padStart(value, 20, ' ');
        }
    };
    
    private final List<String> names = new ArrayList<String>();
    private final List<String> descriptions = new ArrayList<String>();
    private final List<Class<?>> classes = new ArrayList<Class<?>>();
    private final List<Map<String, Object>> values = Lists.newArrayList();

    private InProgressSnapshot(EventTimer timer, ThreadContext context) {
        names.add("Thread name");
        descriptions.add("Thread name");
        classes.add(String.class);

        names.add("Thread ID");
        descriptions.add("Thread ID");
        classes.add(Long.class);

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
                keyedValues.put("Thread ID", entry.getKey().getId());
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
}