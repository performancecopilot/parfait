package io.pcp.parfait.dropwizard.metricadapters;

import static com.codahale.metrics.MetricRegistry.name;
import static tec.uom.se.AbstractUnit.ONE;

import javax.measure.Unit;
import java.util.Set;

import com.codahale.metrics.Histogram;
import io.pcp.parfait.Monitorable;
import io.pcp.parfait.ValueSemantics;
import io.pcp.parfait.dropwizard.MetricAdapter;
import com.google.common.collect.Sets;

public class HistogramAdapter implements MetricAdapter {

    private final SamplingAdapter samplingAdapter;
    private final CountingAdapter countingAdapter;

    public HistogramAdapter(Histogram histogram, String name, String description, Unit<?> unit) {
        this.samplingAdapter = new SamplingAdapter(histogram, name, description, unit);
        this.countingAdapter = new CountingAdapter(histogram, name(name, "count"), description + " - Count", ValueSemantics.MONOTONICALLY_INCREASING);
    }

    public HistogramAdapter(Histogram histogram, String name, String description) {
        this(histogram, name, description, ONE);
    }

    @Override
    public Set<Monitorable> getMonitorables() {
        Set<Monitorable> monitorables = Sets.newHashSet();
        monitorables.addAll(samplingAdapter.getMonitorables());
        monitorables.addAll(countingAdapter.getMonitorables());
        return monitorables;
    }

    @Override
    public void updateMonitorables() {
        samplingAdapter.updateMonitorables();
        countingAdapter.updateMonitorables();
    }
}
