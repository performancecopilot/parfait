package io.pcp.parfait.dropwizard;

import com.codahale.metrics.Metric;

public interface MetricAdapterFactory {

    /**
     * Create a MetricAdapted for a Dropwizard metric
     *
     * @param name The name the metric is published under in Dropwizard
     * @param metric The metric
     * @return A MetricAdapter representing the passed Metric
     */
    MetricAdapter createMetricAdapterFor(String name, Metric metric);
}
