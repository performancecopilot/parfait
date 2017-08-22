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

import javax.measure.Unit;
import java.util.Set;

import io.pcp.parfait.dropwizard.MetricAdapter;
import io.pcp.parfait.dropwizard.NonSelfRegisteringSettableValue;
import com.codahale.metrics.Gauge;
import io.pcp.parfait.Monitorable;
import io.pcp.parfait.ValueSemantics;
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
