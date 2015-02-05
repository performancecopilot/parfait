package com.custardsource.parfait.dropwizard;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Timer;
import com.custardsource.parfait.dropwizard.metricadapters.CountingAdapter;
import com.custardsource.parfait.dropwizard.metricadapters.GaugeAdapter;
import com.custardsource.parfait.dropwizard.metricadapters.HistogramAdapter;
import com.custardsource.parfait.dropwizard.metricadapters.MeteredAdapter;
import com.custardsource.parfait.dropwizard.metricadapters.TimerAdapter;
import com.google.common.base.Preconditions;

public class MetricAdapterFactoryImpl implements MetricAdapterFactory {

    private MetricDescriptorLookup metricDescriptorLookup;
    private MetricNameTranslator metricNameTranslator;

    public MetricAdapterFactoryImpl(MetricDescriptorLookup metricDescriptorLookup, MetricNameTranslator metricNameTranslator) {
        this.metricDescriptorLookup = metricDescriptorLookup;
        this.metricNameTranslator = metricNameTranslator;
    }

    public MetricAdapterFactoryImpl(MetricDescriptorLookup metricDescriptorLookup) {
        this(metricDescriptorLookup, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MetricAdapter createMetricAdapterFor(String originalMetricName, Metric metric) {
        Preconditions.checkArgument(!(metric instanceof MetricSet), "Metric Sets cannot be adapted!!");

        String translatedName = translate(originalMetricName);

        MetricDescriptor descriptor = metricDescriptorLookup.getDescriptorFor(translatedName);

        if (metric instanceof Timer) {
            return new TimerAdapter((Timer) metric, translatedName, descriptor.getDescription());
        }

        if (metric instanceof Histogram) {
            return new HistogramAdapter((Histogram) metric, translatedName, descriptor.getDescription(), descriptor.getUnit());
        }

        if (metric instanceof Counter) {
            return new CountingAdapter((Counter) metric, translatedName, descriptor.getDescription(), descriptor.getSemantics());
        }

        if (metric instanceof Gauge) {
            return new GaugeAdapter<>((Gauge) metric, translatedName, descriptor.getDescription(), descriptor.getUnit(), descriptor.getSemantics());
        }

        if (metric instanceof Metered) {
            return new MeteredAdapter((Metered) metric, translatedName, descriptor.getDescription());
        }

        throw new UnsupportedOperationException(String.format("Unable to produce a monitorable adapter for metrics of class %s (%s)", metric.getClass().getName(), originalMetricName));
    }

    private String translate(String originalName) {
        return metricNameTranslator != null ? metricNameTranslator.translate(originalName) : originalName;
    }
}
