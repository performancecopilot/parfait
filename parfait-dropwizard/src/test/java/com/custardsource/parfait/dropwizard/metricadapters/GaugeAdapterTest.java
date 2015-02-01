package com.custardsource.parfait.dropwizard.metricadapters;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.Unit;

import com.custardsource.parfait.dropwizard.MetricAdapter;
import com.codahale.metrics.Gauge;
import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.ValueSemantics;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GaugeAdapterTest {

    private static final String NAME = "NAME";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final Unit<Dimensionless> UNIT = Unit.ONE;
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
