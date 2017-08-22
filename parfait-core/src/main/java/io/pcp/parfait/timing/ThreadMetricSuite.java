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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ThreadMetricSuite {
    private final List<ThreadMetric> metrics;
    
    private ThreadMetricSuite(Collection<? extends ThreadMetric> metrics) {
        this.metrics = new CopyOnWriteArrayList<ThreadMetric>(metrics);
    }
    
    public final void addMetric(ThreadMetric metric) {
        metrics.add(metric);
    }

    public final void addAllMetrics(Collection<ThreadMetric> metrics) {
        this.metrics.addAll(metrics);
    }
    
    public final List<ThreadMetric> metrics() {
        return Collections.unmodifiableList(metrics);
    }

    public static ThreadMetricSuite blank() {
        return new ThreadMetricSuite(Collections.<ThreadMetric>emptyList());
    }

    public static ThreadMetricSuite withDefaultMetrics() {
        return new ThreadMetricSuite(StandardThreadMetrics.defaults());
    }
}
