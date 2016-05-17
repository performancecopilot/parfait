package com.custardsource.parfait.dropwizard;

import javax.measure.Unit;

import com.custardsource.parfait.ValueSemantics;

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
     * @see com.custardsource.parfait.ValueSemantics
     */
    ValueSemantics getSemantics();
}
