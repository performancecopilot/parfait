package com.custardsource.parfait.timing;


/**
 * A dummy ControllerMetricCollectorFactory which implements all functionality as no-ops.
 */
public final class DummyControllerMetricCollectorFactory extends ControllerMetricCollectorFactory {

    private static final ControllerMetricCollector DUMMY_CONTROLLER_METRIC_COLLECTOR = new ControllerMetricCollector(
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

    public ControllerMetricCollector getCollector() {
        return DUMMY_CONTROLLER_METRIC_COLLECTOR;
    }

    public void addController(MetricCollectorController controller, String beanName) {
        controller.setMetricCollectorFactory(this);
    }

}
