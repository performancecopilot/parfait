package com.custardsource.parfait.dropwizard.metricadapters;

import static com.codahale.metrics.MetricRegistry.name;

import javax.measure.unit.Unit;
import java.util.Set;

import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.ValueSemantics;
import com.custardsource.parfait.dropwizard.MetricAdapter;
import com.custardsource.parfait.dropwizard.NonSelfRegisteringSettableValue;
import com.google.common.collect.Sets;

public class SamplingAdapter implements MetricAdapter {

    private final Sampling samplingMetric;
    private final NonSelfRegisteringSettableValue<Double> mean;
    private final NonSelfRegisteringSettableValue<Long> max;
    private final NonSelfRegisteringSettableValue<Double> median;
    private final NonSelfRegisteringSettableValue<Long> min;
    private final NonSelfRegisteringSettableValue<Double> stddev;

    public SamplingAdapter(Sampling samplingMetric, String name, String description, Unit<?> unit) {
        this.samplingMetric = samplingMetric;
        Snapshot snapshot = samplingMetric.getSnapshot();
        this.mean = new NonSelfRegisteringSettableValue<>(name(name, "mean"), description + " - Mean", unit, snapshot.getMean(), ValueSemantics.FREE_RUNNING);
        this.median = new NonSelfRegisteringSettableValue<>(name(name, "median"), description + " - Median", unit, snapshot.getMedian(), ValueSemantics.FREE_RUNNING);
        this.max = new NonSelfRegisteringSettableValue<>(name(name, "max"), description + " - Maximum", unit, snapshot.getMax(), ValueSemantics.MONOTONICALLY_INCREASING);
        this.min = new NonSelfRegisteringSettableValue<>(name(name, "min"), description + " - Minimum", unit, snapshot.getMin(), ValueSemantics.FREE_RUNNING);
        this.stddev = new NonSelfRegisteringSettableValue<>(name(name, "stddev"), description + " - Standard Deviation", unit, snapshot.getStdDev(), ValueSemantics.FREE_RUNNING);
    }

    public SamplingAdapter(Sampling samplingMetric, String name, String description) {
        this(samplingMetric, name, description, Unit.ONE);
    }

    @Override
    public Set<Monitorable> getMonitorables() {
        return Sets.<Monitorable>newHashSet(mean, median, max, min, stddev);
    }

    @Override
    public void updateMonitorables() {
        Snapshot snapshot = samplingMetric.getSnapshot();
        mean.set(snapshot.getMean());
        median.set(snapshot.getMedian());
        max.set(snapshot.getMax());
        min.set(snapshot.getMin());
        stddev.set(snapshot.getStdDev());
    }
}
