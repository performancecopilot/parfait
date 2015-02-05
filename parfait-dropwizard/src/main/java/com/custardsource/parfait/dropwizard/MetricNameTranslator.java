package com.custardsource.parfait.dropwizard;

/**
 * MetricNameTranslators can be provided to {@link com.custardsource.parfait.dropwizard.MetricAdapterFactoryImpl}
 * to translate the metric names originating from Dropwizard to those that will be published in Parfait.
 */
public interface MetricNameTranslator {

    /**
     * Translate a metric name into the name the metric will be published under in Parfait
     *
     * @param name The metric name provided by Dropwizard
     * @return The name to use to publish the metric in Parfait
     */
    String translate(String name);
}
