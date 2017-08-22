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

/**
 *
 */
package io.pcp.parfait.timing;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class EventCounters {
    private final Map<ThreadMetric, EventMetricCounters> metrics = new LinkedHashMap<ThreadMetric, EventMetricCounters>();
    private final EventMetricCounters invocationCounter;
    private final String eventGroupName;

    public EventCounters(EventMetricCounters invocationCounter, String eventGroupName) {
        this.invocationCounter = invocationCounter;
        this.eventGroupName = eventGroupName;
    }

    public EventMetricCounters getInvocationCounter() {
        return invocationCounter;
    }

    public void addMetric(ThreadMetric metric) {
        addMetric(metric, null);
    }

    public void addMetric(ThreadMetric metric, EventMetricCounters counter) {
        metrics.put(metric, counter);
    }

    Collection<ThreadMetric> getMetricSources() {
        return metrics.keySet();
    }

    EventMetricCounters getCounterForMetric(ThreadMetric metric) {
        return metrics.get(metric);
    }

    Integer numberOfTimerCounters() {
        return metrics.values().size();
    }

    Map<ThreadMetric, EventMetricCounters> getMetrics() {
        return Collections.unmodifiableMap(metrics);
    }

    public String getEventGroupName() {
        return eventGroupName;
    }
}