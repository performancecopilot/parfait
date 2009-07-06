package com.custardsource.parfait.timing;

import com.custardsource.parfait.MonitorableRegistry;


/**
 * A dummy EventTimer which implements all functionality as no-ops.
 */
public final class DummyEventTimer extends EventTimer {
    public DummyEventTimer(MonitorableRegistry registry) {
        super("dummy", registry);
    }

    private static final EventMetricCollector DUMMY_EVENT_METRIC_COLLECTOR = new EventMetricCollector(
            null) {
        public void startTiming(Object source, String action) {
            // no-op
        }

        public void stopTiming() {
            // no-op
        }

        public void pauseForForward() {
            // no-op
        }

        public void resumeAfterForward() {
            // no-op
        }
    };

    public EventMetricCollector getCollector() {
        return DUMMY_EVENT_METRIC_COLLECTOR;
    }

    public void registerTimeable(Timeable timeable, String beanName) {
        timeable.setEventTimer(this);
    }

}
