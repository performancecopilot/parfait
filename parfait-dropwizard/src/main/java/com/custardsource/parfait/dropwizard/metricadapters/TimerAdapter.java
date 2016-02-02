package com.custardsource.parfait.dropwizard.metricadapters;

import com.codahale.metrics.Timer;
import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.dropwizard.MetricAdapter;
import com.google.common.collect.Sets;

import javax.measure.unit.SI;
import java.util.Set;

public class TimerAdapter implements MetricAdapter {

    private final MeteredAdapter meteredAdapter;
    private final SamplingAdapter samplingAdapter;

    public TimerAdapter(Timer timer, String name, String description) {
        meteredAdapter = new MeteredAdapter(timer, name, description);
        samplingAdapter = new SamplingAdapter(timer, name, description, SI.NANO(SI.SECOND));
    }

    @Override
    public Set<Monitorable> getMonitorables() {
        Set<Monitorable> allMonitorables = Sets.newHashSet();
        allMonitorables.addAll(meteredAdapter.getMonitorables());
        allMonitorables.addAll(samplingAdapter.getMonitorables());
        return allMonitorables;
    }

    @Override
    public void updateMonitorables() {
        meteredAdapter.updateMonitorables();
        samplingAdapter.updateMonitorables();
    }
}
