package com.custardsource.parfait.dropwizard.metricadapters;

import javax.measure.unit.Unit;
import java.util.Set;

import com.custardsource.parfait.dropwizard.MetricAdapter;
import com.custardsource.parfait.dropwizard.NonSelfRegisteringSettableValue;
import com.codahale.metrics.Counting;
import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.ValueSemantics;
import com.google.common.collect.Sets;

public class CountingAdapter implements MetricAdapter {

    private final Counting counter;
    private final NonSelfRegisteringSettableValue<Long> monitoredValue;

    public CountingAdapter(Counting counter, String name, String description, ValueSemantics valueSemantics) {
        this.counter = counter;
        this.monitoredValue = new NonSelfRegisteringSettableValue<>(name, description, Unit.ONE, counter.getCount(), valueSemantics);
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
