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
