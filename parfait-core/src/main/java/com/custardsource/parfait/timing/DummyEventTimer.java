package com.custardsource.parfait.timing;

import java.util.Collections;

import com.custardsource.parfait.MonitorableRegistry;


/**
 * A dummy EventTimer which implements all functionality as no-ops.
 */
public final class DummyEventTimer extends EventTimer {
    public DummyEventTimer() {
        super("dummy", new MonitorableRegistry(), ThreadMetricSuite.blank(), false, false,
                Collections.<StepMeasurementSink>emptyList());
    }

    private static final EventMetricCollector DUMMY_EVENT_METRIC_COLLECTOR = new EventMetricCollector(
            null, Collections.<StepMeasurementSink>emptyList()) {
        @Override
        public void startTiming(Object eventGroup, String event) {
            // no-op
        }

        @Override
        public void stopTiming() {
            // no-op
        }

        @Override
        public void pauseForForward() {
            // no-op
        }

        @Override
        public void resumeAfterForward() {
            // no-op
        }
    };

    public EventMetricCollector getCollector() {
        return DUMMY_EVENT_METRIC_COLLECTOR;
    }

    public void registerTimeable(Timeable timeable, String eventGroup) {
        timeable.setEventTimer(this);
    }

}
