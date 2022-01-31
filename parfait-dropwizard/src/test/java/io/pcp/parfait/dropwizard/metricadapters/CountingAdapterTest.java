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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import javax.measure.Unit;

import io.pcp.parfait.dropwizard.MetricAdapter;
import com.codahale.metrics.Counting;
import io.pcp.parfait.Monitorable;
import io.pcp.parfait.ValueSemantics;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CountingAdapterTest {

    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String NAME = "NAME";
    private static final long INITIAL_VALUE = 123123;

    @Mock
    private Counting counter;
    private CountingAdapter metricAdapter;

    @Before
    public void setUp() {
        when(counter.getCount()).thenReturn(INITIAL_VALUE);
        metricAdapter = new CountingAdapter(counter, NAME, DESCRIPTION, ValueSemantics.FREE_RUNNING);
    }

    @Test
    public void shouldReturnLongAsType() {
        assertThat(getFirstMonitorable(metricAdapter).getType(), Matchers.<Class>equalTo(Long.class));
    }

    @Test
    public void shouldReportValueSemanticsAsFreeRunning() {
        assertThat(getFirstMonitorable(metricAdapter).getSemantics(), is(ValueSemantics.FREE_RUNNING));
    }

    @Test
    public void shouldReportValueSemanticsAsMonitonicallyIncreasing() {
        assertThat(getFirstMonitorable(new CountingAdapter(counter, NAME, DESCRIPTION, ValueSemantics.MONOTONICALLY_INCREASING)).getSemantics(), is(ValueSemantics.MONOTONICALLY_INCREASING));
    }

    @Test
    public void shouldReturnOneAsUnitOfMeasurement() {
        assertThat(getFirstMonitorable(metricAdapter).getUnit(), Matchers.<Unit>is(ONE));
    }

    @Test
    public void shouldReturnSpecifiedDescription() {
        assertThat(getFirstMonitorable(metricAdapter).getDescription(), is(DESCRIPTION));
    }

    @Test
    public void shouldReturnSpecifiedName() {
        assertThat(getFirstMonitorable(metricAdapter).getName(), is(NAME));
    }

    @Test
    public void shouldDelegateToCounterToReturnCurrentValue() {
        assertThat(getFirstMonitorable(metricAdapter).get(), Matchers.<Object>is(INITIAL_VALUE));
    }

    @Test
    public void shouldUpdateMonitorValueWhenUpdateMonitorsIsCalled() {
        final long newValue = INITIAL_VALUE + 5;
        when(counter.getCount()).thenReturn(newValue);
        CountingAdapter countingAdapter = metricAdapter;
        countingAdapter.updateMonitorables();
        assertThat(getFirstMonitorable(countingAdapter).get(), Matchers.<Object>is(newValue));
    }

    private Monitorable getFirstMonitorable(MetricAdapter metricAdapter) {
        return metricAdapter.getMonitorables().iterator().next();
    }
}
