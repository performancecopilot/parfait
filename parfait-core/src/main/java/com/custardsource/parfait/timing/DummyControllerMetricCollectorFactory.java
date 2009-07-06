package com.custardsource.parfait.timing;


/**
 * A dummy ControllerMetricCollectorFactory which implements all functionality as no-ops.
 */
public final class DummyControllerMetricCollectorFactory extends EventTimer {

    private static final EventMetricCollector DUMMY_CONTROLLER_METRIC_COLLECTOR = new EventMetricCollector(
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
        return DUMMY_CONTROLLER_METRIC_COLLECTOR;
    }

    public void addController(Timeable controller, String beanName) {
        controller.setMetricCollectorFactory(this);
    }

}
