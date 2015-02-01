package com.custardsource.parfait.dropwizard;

/**
 * The strategy used to retrieve the metric descriptor for metrics being adapted
 */
public interface MetricDescriptorLookup {

    /**
     * Get the metric descriptor for the specified metric
     *
     * @param metricName The name the metric is publish under in Dropwizard
     * @return The MetricDescriptor for that metric
     */
    MetricDescriptor getDescriptorFor(String metricName);
}
