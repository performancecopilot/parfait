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

import static tech.units.indriya.AbstractUnit.ONE;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import javax.measure.quantity.Dimensionless;
import javax.measure.Unit;

import io.pcp.parfait.dropwizard.MetricAdapter;
import com.codahale.metrics.Gauge;
import io.pcp.parfait.Monitorable;
import io.pcp.parfait.ValueSemantics;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GaugeAdapterTest {

    private static final String NAME = "NAME";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final Unit<Dimensionless> UNIT = ONE;
    private static final ValueSemantics VALUE_SEMANTICS = ValueSemantics.FREE_RUNNING;
    private static final int INITIAL_VALUE = 123215431;

    @Mock
    private Gauge<Integer> gauge;

    private GaugeAdapter<Integer> gaugeAdapter;

    @Before
    public void setUp() {
        Integer gaugeValue = INITIAL_VALUE;
        when(gauge.getValue()).thenReturn(gaugeValue);
        gaugeAdapter = new GaugeAdapter<>(gauge, NAME, DESCRIPTION, UNIT, VALUE_SEMANTICS);
    }

    @Test
    public void shouldReturnSpecifiedName() {
        assertThat(getFirstMonitorable(gaugeAdapter).getName(), is(NAME));
    }

    @Test
    public void shouldReturnSpecifiedDescription() {
        assertThat(getFirstMonitorable(gaugeAdapter).getDescription(), is(DESCRIPTION));
    }

    @Test
    public void shouldReturnSpecifiedUnitOfMeasurement() {
        assertThat(getFirstMonitorable(gaugeAdapter).getUnit(), Matchers.<Unit>is(UNIT));
    }

    @Test
    public void shouldReturnSpecifiedValueSemantics() {
        assertThat(getFirstMonitorable(gaugeAdapter).getSemantics(), is(VALUE_SEMANTICS));
    }

    @Test
    public void shouldDelegateToGaugeToReturnCurrentValue() {
        assertThat((Integer)getFirstMonitorable(gaugeAdapter).get(), is(INITIAL_VALUE));
    }

    @Test
    public void shouldUpdateMonitorToCurrentValueWhenUpdateMonitorsIsCalled() {
        int newValue = INITIAL_VALUE + 5;
        when(gauge.getValue()).thenReturn(newValue);
        gaugeAdapter.updateMonitorables();
        assertThat((Integer)getFirstMonitorable(gaugeAdapter).get(), is(newValue));
    }

    private Monitorable getFirstMonitorable(MetricAdapter metricAdapter) {
        return metricAdapter.getMonitorables().iterator().next();
    }
}
