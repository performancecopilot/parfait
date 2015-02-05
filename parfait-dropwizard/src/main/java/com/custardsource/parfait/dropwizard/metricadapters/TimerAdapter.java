package com.custardsource.parfait.dropwizard.metricadapters;

import static com.codahale.metrics.MetricRegistry.name;

import javax.measure.unit.SI;
import java.util.Set;

import com.codahale.metrics.Timer;
import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.ValueSemantics;
import com.custardsource.parfait.dropwizard.MetricAdapter;
import com.google.common.collect.Sets;

public class TimerAdapter implements MetricAdapter {

    private final MeteredAdapter meteredAdapter;
    private final SamplingAdapter samplingAdapter;
    private final CountingAdapter countingAdapter;

    public TimerAdapter(Timer timer, String name, String description) {
        meteredAdapter = new MeteredAdapter(timer, name, description);
        samplingAdapter = new SamplingAdapter(timer, name, description, SI.NANO(SI.SECOND));
        countingAdapter = new CountingAdapter(timer, name(name, "count"), description + " - Count", ValueSemantics.MONOTONICALLY_INCREASING);
    }

    @Override
    public Set<Monitorable> getMonitorables() {
        Set<Monitorable> allMonitorables = Sets.newHashSet();
        allMonitorables.addAll(meteredAdapter.getMonitorables());
        allMonitorables.addAll(samplingAdapter.getMonitorables());
        allMonitorables.addAll(countingAdapter.getMonitorables());
        return allMonitorables;
    }

    @Override
    public void updateMonitorables() {
        meteredAdapter.updateMonitorables();
        samplingAdapter.updateMonitorables();
        countingAdapter.updateMonitorables();
    }
}
