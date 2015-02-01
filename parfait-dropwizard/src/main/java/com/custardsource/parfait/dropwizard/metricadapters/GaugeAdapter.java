package com.custardsource.parfait.dropwizard.metricadapters;

import javax.measure.unit.Unit;
import java.util.Set;

import com.custardsource.parfait.dropwizard.MetricAdapter;
import com.custardsource.parfait.dropwizard.NonSelfRegisteringSettableValue;
import com.codahale.metrics.Gauge;
import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.ValueSemantics;
import com.google.common.collect.Sets;

public class GaugeAdapter<T> implements MetricAdapter {

    private final Gauge<T> theGauge;
    private final NonSelfRegisteringSettableValue<T> monitoredValue;

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public GaugeAdapter(Gauge<T> theGauge, String name, String description, Unit<?> unit, ValueSemantics valueSemantics) {
        this.theGauge = theGauge;
        this.monitoredValue = new NonSelfRegisteringSettableValue<>(name, description, unit, theGauge.getValue(), valueSemantics);
    }

    @Override
    public Set<Monitorable> getMonitorables() {
        return Sets.<Monitorable>newHashSet(monitoredValue);
    }

    @Override
    public void updateMonitorables() {
        monitoredValue.set(theGauge.getValue());
    }
}
