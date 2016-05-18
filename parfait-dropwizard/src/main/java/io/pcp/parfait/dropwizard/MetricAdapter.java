package io.pcp.parfait.dropwizard;

import java.util.Set;

import io.pcp.parfait.Monitorable;

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
