package com.custardsource.parfait.timing;

import org.springframework.stereotype.Controller;

/**
 * A {@link Controller} that implements some form of metric collection via a
 * {@link ControllerMetricCollectorFactory}.
 */
public interface MetricCollectorController {

    public void setMetricCollectorFactory(ControllerMetricCollectorFactory metricCollectorFactory);

}
