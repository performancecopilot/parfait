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

import static tec.uom.se.unit.MetricPrefix.NANO;
import static tec.uom.se.unit.Units.SECOND;

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
