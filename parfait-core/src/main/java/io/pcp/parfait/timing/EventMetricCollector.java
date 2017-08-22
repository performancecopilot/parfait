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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Coordinates multiple {@link MetricMeasurement MetricMeasurements} for all the events invoked in
 * the process of handling the user request.
 * </p>
 * <p>
 * Not thread-safe, should be used only by one thread at a time (obtaining the top-level measurement
 * via {@link #getInProgressMeasurements()} is thread-safe and permitted).
 * </p>
 */
public class EventMetricCollector {
    private volatile StepMeasurements top = null;

    private StepMeasurements current = null;
    /**
     * The number of nested events invoked so far. When we hit depth of 0 we know we've reached
     * the top-level event requested by the user.
     */
    private int depth = 0;
    private Deque<Object> topLevelEvent = new ArrayDeque<Object>();

    private final Map<Object, EventCounters> perEventCounters;
    private final List<StepMeasurementSink> sinks;

    EventMetricCollector(Map<Object, EventCounters> perEventCounters,
            List<StepMeasurementSink> measurementSinks) {
        this.perEventCounters = perEventCounters;
        // Our creator keeps an immutable copy of this so we don't need to defensively clone
        this.sinks = measurementSinks;
    }

    public void startTiming(Object eventGroup, String event) {
        StepMeasurements newTiming = new StepMeasurements(current, perEventCounters.get(eventGroup).getEventGroupName(),
                event);
        for (ThreadMetric metric : perEventCounters.get(eventGroup).getMetricSources()) {
            newTiming.addMetricInstance(new MetricMeasurement(metric, Thread.currentThread()));
        }
        current = newTiming;
        topLevelEvent.addFirst(eventGroup);
        depth++;
        if (top == null) {
            top = newTiming;
        }
        current.startAll();
    }

    public void stopTiming() {
        current.stopAll();
        depth--;
        Object event = topLevelEvent.removeFirst();

        for (StepMeasurementSink sink : sinks) {
            sink.handle(current, depth);
        }

        if (depth == 0 && perEventCounters.containsKey(event)) {
            // We're at the top level, increment our event counters too
            EventCounters counters = perEventCounters.get(event);
            for (MetricMeasurement metric : current.getMetricInstances()) {
                EventMetricCounters counter = counters.getCounterForMetric(metric
                        .getMetricSource());
                if (counter != null) {
                    // We have potential race conditions here in that some metrics (e.g.
                    // SYSTEM_CPU_TIME) cannot be calculated atomically, as they are derived from 2
                    // non-atomic measurements. Depending on when the kernel counter increments, we
                    // may see spurious negative values on such derived metrics. We clip to 0 to
                    // avoid this. Example: at the start of a thread execution, we may get total
                    // cpu=1000ms, user=800ms, system is calculated as 200ms. At completion,
                    // total=1000ms, user=810ms (only user has 'ticked' over), system calculated as
                    // 190ms. We spuriously think that the individual request has taken
                    // (190 - 200) = -10ms.
                    counter.incrementCounters(Math.max(metric.totalValue().getValue().longValue(), 0L));
                }
            }
            counters.getInvocationCounter().incrementCounters(1);
        }
        current = current.getParent();
        if (depth == 0) {
            top = null;
        }
    }

    public void pauseForForward() {
        current.pauseAll();
    }

    public void resumeAfterForward() {
        current.resumeAll();
    }

    final StepMeasurements getInProgressMeasurements() {
        return top;
    }
}
