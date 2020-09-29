/*
 * Copyright 2009-2017 Aconex
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.pcp.parfait.dropwizard.metricadapters;

import static com.codahale.metrics.MetricRegistry.name;
import static tech.units.indriya.AbstractUnit.ONE;

import javax.measure.Unit;
import java.util.Set;

import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import io.pcp.parfait.Monitorable;
import io.pcp.parfait.ValueSemantics;
import io.pcp.parfait.dropwizard.MetricAdapter;
import io.pcp.parfait.dropwizard.NonSelfRegisteringSettableValue;
import com.google.common.collect.Sets;

public class SamplingAdapter implements MetricAdapter {

    private final Sampling samplingMetric;
    private final NonSelfRegisteringSettableValue<Double> mean;
    private final NonSelfRegisteringSettableValue<Long> max;
    private final NonSelfRegisteringSettableValue<Double> median;
    private final NonSelfRegisteringSettableValue<Long> min;
    private final NonSelfRegisteringSettableValue<Double> stddev;
    private final NonSelfRegisteringSettableValue<Double> seventyFifthPercentile;
    private final NonSelfRegisteringSettableValue<Double> ninetyFifthPercentile;
    private final NonSelfRegisteringSettableValue<Double> ninetyEighthPercentile;
    private final NonSelfRegisteringSettableValue<Double> ninetyNinthPercentile;
    private final NonSelfRegisteringSettableValue<Double> threeNinesPercentile;


    public SamplingAdapter(Sampling samplingMetric, String name, String description, Unit<?> unit) {
        this.samplingMetric = samplingMetric;
        Snapshot snapshot = samplingMetric.getSnapshot();
        this.mean = new NonSelfRegisteringSettableValue<>(name(name, "mean"), description + " - Mean", unit, snapshot.getMean(), ValueSemantics.FREE_RUNNING);
        this.seventyFifthPercentile = new NonSelfRegisteringSettableValue<>(name(name, "seventyfifth"), description + " - 75th Percentile of recent data", unit, snapshot.get75thPercentile(), ValueSemantics.FREE_RUNNING);
        this.ninetyFifthPercentile = new NonSelfRegisteringSettableValue<>(name(name, "ninetyfifth"), description + " - 95th Percentile of recent data", unit, snapshot.get95thPercentile(), ValueSemantics.FREE_RUNNING);
        this.ninetyEighthPercentile = new NonSelfRegisteringSettableValue<>(name(name, "ninetyeighth"), description + " - 98th Percentile of recent data", unit, snapshot.get98thPercentile(), ValueSemantics.FREE_RUNNING);
        this.ninetyNinthPercentile = new NonSelfRegisteringSettableValue<>(name(name, "ninetynineth"), description + " - 99th Percentile of recent data", unit, snapshot.get99thPercentile(), ValueSemantics.FREE_RUNNING);
        this.threeNinesPercentile = new NonSelfRegisteringSettableValue<>(name(name, "threenines"), description + " - 99.9th Percentile of recent data", unit, snapshot.get999thPercentile(), ValueSemantics.FREE_RUNNING);

        this.median = new NonSelfRegisteringSettableValue<>(name(name, "median"), description + " - Median", unit, snapshot.getMedian(), ValueSemantics.FREE_RUNNING);
        this.max = new NonSelfRegisteringSettableValue<>(name(name, "max"), description + " - Maximum", unit, snapshot.getMax(), ValueSemantics.MONOTONICALLY_INCREASING);
        this.min = new NonSelfRegisteringSettableValue<>(name(name, "min"), description + " - Minimum", unit, snapshot.getMin(), ValueSemantics.FREE_RUNNING);
        this.stddev = new NonSelfRegisteringSettableValue<>(name(name, "stddev"), description + " - Standard Deviation", unit, snapshot.getStdDev(), ValueSemantics.FREE_RUNNING);
    }

    public SamplingAdapter(Sampling samplingMetric, String name, String description) {
        this(samplingMetric, name, description, ONE);
    }

    @Override
    public Set<Monitorable> getMonitorables() {
        return Sets.<Monitorable>newHashSet(mean, median, max, min, stddev, seventyFifthPercentile, ninetyFifthPercentile, ninetyEighthPercentile, ninetyNinthPercentile, threeNinesPercentile);
    }

    @Override
    public void updateMonitorables() {
        Snapshot snapshot = samplingMetric.getSnapshot();
        mean.set(snapshot.getMean());
        median.set(snapshot.getMedian());
        max.set(snapshot.getMax());
        min.set(snapshot.getMin());
        stddev.set(snapshot.getStdDev());
        seventyFifthPercentile.set(snapshot.get75thPercentile());
        ninetyFifthPercentile.set(snapshot.get95thPercentile());
        ninetyEighthPercentile.set(snapshot.get98thPercentile());
        ninetyNinthPercentile.set(snapshot.get99thPercentile());
        threeNinesPercentile.set(snapshot.get999thPercentile());

    }
}
