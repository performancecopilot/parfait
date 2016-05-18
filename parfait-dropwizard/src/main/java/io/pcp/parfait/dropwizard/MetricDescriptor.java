package io.pcp.parfait.dropwizard;

import javax.measure.Unit;

import io.pcp.parfait.ValueSemantics;

/**
 * The metadata published with a metric in Parfait
 */
public interface MetricDescriptor {

    /**
     * The unit of the metric
     *
     * @return The JSR-363 unit
     */
    Unit getUnit();

    /**
     * The human-readable description of the metric
     *
     * @return The description
     */
    String getDescription();

    /**
     * The ValueSemantics of the metric
     *
     * @return the ValueSemantics
     * @see io.pcp.parfait.ValueSemantics
     */
    ValueSemantics getSemantics();
}
