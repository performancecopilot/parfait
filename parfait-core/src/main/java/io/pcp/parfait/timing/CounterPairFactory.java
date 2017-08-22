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

import io.pcp.parfait.*;

import javax.measure.Unit;

public class CounterPairFactory {
    private final MonitorableRegistry registry;
    private final ThreadMetricSuite metricSuite;

    public CounterPairFactory(MonitorableRegistry registry, ThreadMetricSuite metricSuite) {
        this.registry = registry;
        this.metricSuite = metricSuite;
    }

    public Counter createCounterPair(Unit<?> unit, String globalCounterName, String threadMetricName,
            String threadMetricSuffix, String description) {
        MonitoredCounter metric = new MonitoredCounter(globalCounterName, description, registry, unit);
        ThreadCounter threadCounter = new ThreadCounter.ThreadMapCounter();

        ThreadMetric threadMetric = new ThreadValueMetric(threadMetricName, unit, threadMetricSuffix,
                description, threadCounter);
        metricSuite.addMetric(threadMetric);
        return new CounterPair(metric, threadCounter);
    }

}
