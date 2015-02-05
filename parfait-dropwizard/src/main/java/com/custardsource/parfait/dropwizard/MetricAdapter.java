package com.custardsource.parfait.dropwizard;

import java.util.Set;

import com.custardsource.parfait.Monitorable;

/**
 * An adapter for a Dropwizard metric
 */
public interface MetricAdapter {

    /**
     * Get the set of Monitorables for this Metric
     */
    Set<Monitorable> getMonitorables();

    /**
     * Update the Monitorable(s) from the Metric
     */
    void updateMonitorables();
}
