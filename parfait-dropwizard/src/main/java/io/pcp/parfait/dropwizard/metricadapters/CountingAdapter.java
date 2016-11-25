package io.pcp.parfait.dropwizard.metricadapters;

import static tec.uom.se.AbstractUnit.ONE;

import java.util.Set;

import io.pcp.parfait.dropwizard.MetricAdapter;
import io.pcp.parfait.dropwizard.NonSelfRegisteringSettableValue;
import com.codahale.metrics.Counting;
import io.pcp.parfait.Monitorable;
import io.pcp.parfait.ValueSemantics;
import com.google.common.collect.Sets;

public class CountingAdapter implements MetricAdapter {

    private final Counting counter;
    private final NonSelfRegisteringSettableValue<Long> monitoredValue;

    public CountingAdapter(Counting counter, String name, String description, ValueSemantics valueSemantics) {
        this.counter = counter;
        this.monitoredValue = new NonSelfRegisteringSettableValue<>(name, description, ONE, counter.getCount(), valueSemantics);
    }

    @Override
    public Set<Monitorable> getMonitorables() {
        return Sets.<Monitorable>newHashSet(monitoredValue);
    }

    @Override
    public void updateMonitorables() {
        monitoredValue.set(counter.getCount());
    }
}
