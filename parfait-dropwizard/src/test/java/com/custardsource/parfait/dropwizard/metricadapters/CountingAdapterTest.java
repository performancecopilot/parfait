package com.custardsource.parfait.dropwizard.metricadapters;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import javax.measure.unit.Unit;

import com.custardsource.parfait.dropwizard.MetricAdapter;
import com.codahale.metrics.Counting;
import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.ValueSemantics;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
        assertThat(getFirstMonitorable(metricAdapter).getUnit(), Matchers.<Unit>is(Unit.ONE));
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
