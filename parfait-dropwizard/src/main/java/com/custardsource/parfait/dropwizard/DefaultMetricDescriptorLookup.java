package com.custardsource.parfait.dropwizard;

import com.custardsource.parfait.ValueSemantics;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.Unit;

public class DefaultMetricDescriptorLookup implements MetricDescriptorLookup {

    @Override
    public MetricDescriptor getDescriptorFor(final String metricName) {
        return new MetricDescriptor() {
            @Override
            public Unit<Dimensionless> getUnit() {
                return Unit.ONE;
            }

            @Override
            public String getDescription() {
                return metricName;
            }

            @Override
            public ValueSemantics getSemantics() {
                return ValueSemantics.FREE_RUNNING;
            }
        };
    }
}
