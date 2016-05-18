package io.pcp.parfait.dropwizard.metricadapters;

import static tec.units.ri.unit.MetricPrefix.NANO;
import static tec.units.ri.unit.Units.SECOND;

import com.codahale.metrics.Timer;
import io.pcp.parfait.Monitorable;
import io.pcp.parfait.dropwizard.MetricAdapter;
import com.google.common.collect.Sets;

import java.util.Set;

public class TimerAdapter implements MetricAdapter {

    private final MeteredAdapter meteredAdapter;
    private final SamplingAdapter samplingAdapter;

    public TimerAdapter(Timer timer, String name, String description) {
        meteredAdapter = new MeteredAdapter(timer, name, description);
        samplingAdapter = new SamplingAdapter(timer, name, description, NANO(SECOND));
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
