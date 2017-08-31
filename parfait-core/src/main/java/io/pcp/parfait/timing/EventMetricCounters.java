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

import io.pcp.parfait.MonitoredCounter;
import com.google.common.base.Preconditions;

/**
 * This class is a wrapper class which holds both a counter for an event metric and another
 * counter for the same metric but its value is a total value across all events. It is
 * important to ensure that the total counter is the same instance of the class across all
 * EventMetricCounters objects which are measuring the same metric.
 */
public class EventMetricCounters {
    private final MonitoredCounter eventSpecificCounter;
    private final MonitoredCounter totalCounter;

    public EventMetricCounters(MonitoredCounter eventSpecificCounter, MonitoredCounter totalCounter) {
        this.eventSpecificCounter = Preconditions.checkNotNull(eventSpecificCounter,
                "Cannot provide null event-specific metric counter");
        this.totalCounter = Preconditions.checkNotNull(totalCounter,
                "Cannot provide null total metric counter");
    }

    public void incrementCounters(long value) {
        eventSpecificCounter.inc(value);
        totalCounter.inc(value);

    }

    MonitoredCounter getTotalCounter() {
        return totalCounter;
    }

}
