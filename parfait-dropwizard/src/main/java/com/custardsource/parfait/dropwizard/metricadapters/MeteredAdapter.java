package com.custardsource.parfait.dropwizard.metricadapters;

import static tec.units.ri.AbstractUnit.ONE;

import com.codahale.metrics.Metered;
import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.ValueSemantics;
import com.custardsource.parfait.dropwizard.MetricAdapter;
import com.custardsource.parfait.dropwizard.NonSelfRegisteringSettableValue;
import com.google.common.collect.Sets;

import javax.measure.Unit;
import java.util.Set;

import static com.codahale.metrics.MetricRegistry.name;

public class MeteredAdapter implements MetricAdapter {

    private final Metered metered;
    private final NonSelfRegisteringSettableValue<Double> fiveMinuteRate;
    private final NonSelfRegisteringSettableValue<Double> oneMinuteRate;
    private final NonSelfRegisteringSettableValue<Double> meanRate;
    private final NonSelfRegisteringSettableValue<Double> fifteenMinuteRate;
    private final NonSelfRegisteringSettableValue<Long> count;

    public MeteredAdapter(Metered metered, String name, String description) {
        this.metered = metered;
        this.fifteenMinuteRate = new NonSelfRegisteringSettableValue<>(name(name, "fifteen_minute_rate"), description + " - Fifteen minute rate", ONE, metered.getFifteenMinuteRate(), ValueSemantics.FREE_RUNNING);
        this.fiveMinuteRate = new NonSelfRegisteringSettableValue<>(name(name, "five_minute_rate"), description + " - Five minute rate", ONE, metered.getFiveMinuteRate(), ValueSemantics.FREE_RUNNING);
        this.oneMinuteRate = new NonSelfRegisteringSettableValue<>(name(name, "one_minute_rate"), description + " - One minute rate", ONE, metered.getOneMinuteRate(), ValueSemantics.FREE_RUNNING);
        this.meanRate = new NonSelfRegisteringSettableValue<>(name(name, "mean_rate"), description + " - Mean rate", ONE, metered.getMeanRate(), ValueSemantics.FREE_RUNNING);
        this.count = new NonSelfRegisteringSettableValue<>(name(name, "count"), description + " - Count", ONE, metered.getCount(), ValueSemantics.MONOTONICALLY_INCREASING);
    }

    @Override
    public Set<Monitorable> getMonitorables() {
        return Sets.<Monitorable>newHashSet(fifteenMinuteRate, fiveMinuteRate, oneMinuteRate, meanRate, count);
    }

    @Override
    public void updateMonitorables() {
        fifteenMinuteRate.set(metered.getFifteenMinuteRate());
        fiveMinuteRate.set(metered.getFiveMinuteRate());
        oneMinuteRate.set(metered.getOneMinuteRate());
        meanRate.set(metered.getMeanRate());
        count.set(metered.getCount());
    }
}
