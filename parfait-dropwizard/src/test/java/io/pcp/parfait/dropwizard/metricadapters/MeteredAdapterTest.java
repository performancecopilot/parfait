package io.pcp.parfait.dropwizard.metricadapters;

import static tec.uom.se.AbstractUnit.ONE;

import com.codahale.metrics.Metered;
import io.pcp.parfait.Monitorable;
import io.pcp.parfait.ValueSemantics;
import io.pcp.parfait.dropwizard.MetricAdapter;
import com.google.common.collect.Maps;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.measure.Unit;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeteredAdapterTest {

    public static final String NAME = "NAME";
    public static final String DESCRIPTION = "DESCRIPTION";

    private static final double INITIAL_FIFTEEN_MINUTE_RATE = 1.1;
    private static final double INITIAL_FIVE_MINUTE_RATE = 2.2;
    private static final double INITIAL_ONE_MINUTE_RATE = 3.3;
    private static final double INITIAL_MEAN_RATE = 4.4;
    private static final long INITIAL_COUNT = 42;
    private static final String FIFTEEN_MINUTE_RATE = "fifteen_minute_rate";
    private static final String FIVE_MINUTE_RATE = "five_minute_rate";
    private static final String ONE_MINUTE_RATE = "one_minute_rate";
    private static final String MEAN_RATE = "mean_rate";
    private static final String COUNT = "count";

    @Mock
    private Metered metered;

    private MeteredAdapter meteredAdapter;

    @Before
    public void setUp() {
        when(metered.getFifteenMinuteRate()).thenReturn(INITIAL_FIFTEEN_MINUTE_RATE);
        when(metered.getFiveMinuteRate()).thenReturn(INITIAL_FIVE_MINUTE_RATE);
        when(metered.getOneMinuteRate()).thenReturn(INITIAL_ONE_MINUTE_RATE);
        when(metered.getMeanRate()).thenReturn(INITIAL_MEAN_RATE);
        when(metered.getCount()).thenReturn(INITIAL_COUNT);

        meteredAdapter = new MeteredAdapter(metered, NAME, DESCRIPTION);
    }

    @Test
    public void shouldPublishFifteenMinuteRateMetricInNanoseconds() {
        assertThat(extractMonitorables(meteredAdapter).get(FIFTEEN_MINUTE_RATE), notNullValue());
        assertThat(extractMonitorables(meteredAdapter).get(FIFTEEN_MINUTE_RATE).getDescription(), is(DESCRIPTION + " - Fifteen minute rate"));
        assertThat(extractMonitorables(meteredAdapter).get(FIFTEEN_MINUTE_RATE).get(), Matchers.<Object>is(INITIAL_FIFTEEN_MINUTE_RATE));
        assertThat(extractMonitorables(meteredAdapter).get(FIFTEEN_MINUTE_RATE).getSemantics(), is(ValueSemantics.FREE_RUNNING));
        assertThat(extractMonitorables(meteredAdapter).get(FIFTEEN_MINUTE_RATE).getUnit(), Matchers.<Unit>is(ONE));
    }

    @Test
    public void shouldPublishFiveMinuteRateMetric() {
        assertThat(extractMonitorables(meteredAdapter).get(FIVE_MINUTE_RATE), notNullValue());
        assertThat(extractMonitorables(meteredAdapter).get(FIVE_MINUTE_RATE).getDescription(), is(DESCRIPTION + " - Five minute rate"));
        assertThat(extractMonitorables(meteredAdapter).get(FIVE_MINUTE_RATE).get(), Matchers.<Object>is(INITIAL_FIVE_MINUTE_RATE));
        assertThat(extractMonitorables(meteredAdapter).get(FIVE_MINUTE_RATE).getSemantics(), is(ValueSemantics.FREE_RUNNING));
        assertThat(extractMonitorables(meteredAdapter).get(FIVE_MINUTE_RATE).getUnit(), Matchers.<Unit>is(ONE));
    }

    @Test
    public void shouldPublishOneMinuteRateMetric() {
        assertThat(extractMonitorables(meteredAdapter).get(ONE_MINUTE_RATE), notNullValue());
        assertThat(extractMonitorables(meteredAdapter).get(ONE_MINUTE_RATE).getDescription(), is(DESCRIPTION + " - One minute rate"));
        assertThat(extractMonitorables(meteredAdapter).get(ONE_MINUTE_RATE).get(), Matchers.<Object>is(INITIAL_ONE_MINUTE_RATE));
        assertThat(extractMonitorables(meteredAdapter).get(ONE_MINUTE_RATE).getSemantics(), is(ValueSemantics.FREE_RUNNING));
        assertThat(extractMonitorables(meteredAdapter).get(ONE_MINUTE_RATE).getUnit(), Matchers.<Unit>is(ONE));
    }

    @Test
    public void shouldPublishMeanRateMetric() {
        assertThat(extractMonitorables(meteredAdapter).get(MEAN_RATE), notNullValue());
        assertThat(extractMonitorables(meteredAdapter).get(MEAN_RATE).getDescription(), is(DESCRIPTION + " - Mean rate"));
        assertThat(extractMonitorables(meteredAdapter).get(MEAN_RATE).get(), Matchers.<Object>is(INITIAL_MEAN_RATE));
        assertThat(extractMonitorables(meteredAdapter).get(MEAN_RATE).getSemantics(), is(ValueSemantics.FREE_RUNNING));
        assertThat(extractMonitorables(meteredAdapter).get(MEAN_RATE).getUnit(), Matchers.<Unit>is(ONE));
    }

    @Test
    public void shouldPublishCountMetric() {
        assertThat(extractMonitorables(meteredAdapter).get(COUNT), notNullValue());
        assertThat(extractMonitorables(meteredAdapter).get(COUNT).getDescription(), is(DESCRIPTION + " - Count"));
        assertThat(extractMonitorables(meteredAdapter).get(COUNT).get(), Matchers.<Object>is(INITIAL_COUNT));
        assertThat(extractMonitorables(meteredAdapter).get(COUNT).getSemantics(), is(ValueSemantics.MONOTONICALLY_INCREASING));
        assertThat(extractMonitorables(meteredAdapter).get(COUNT).getUnit(), Matchers.<Unit>is(ONE));
    }

    @Test
    public void shouldUpdateAllMonitorables() {
        long updatedCount = INITIAL_COUNT + 1;
        double updatedFifteenMinuteRate = INITIAL_FIFTEEN_MINUTE_RATE * 2.0;
        double updatedFiveMinuteRate = INITIAL_FIVE_MINUTE_RATE * 2.0;
        double updatedOneMinuteRate = INITIAL_ONE_MINUTE_RATE * 2.0;
        double updatedMeanRate = INITIAL_MEAN_RATE * 2.0;

        when(metered.getCount()).thenReturn(updatedCount);
        when(metered.getFifteenMinuteRate()).thenReturn(updatedFifteenMinuteRate);
        when(metered.getFiveMinuteRate()).thenReturn(updatedFiveMinuteRate);
        when(metered.getOneMinuteRate()).thenReturn(updatedOneMinuteRate);
        when(metered.getMeanRate()).thenReturn(updatedMeanRate);

        meteredAdapter.updateMonitorables();

        assertThat(extractMonitorables(meteredAdapter).get(COUNT).get(), Matchers.<Object>is(updatedCount));
        assertThat(extractMonitorables(meteredAdapter).get(FIFTEEN_MINUTE_RATE).get(), Matchers.<Object>is(updatedFifteenMinuteRate));
        assertThat(extractMonitorables(meteredAdapter).get(FIVE_MINUTE_RATE).get(), Matchers.<Object>is(updatedFiveMinuteRate));
        assertThat(extractMonitorables(meteredAdapter).get(ONE_MINUTE_RATE).get(), Matchers.<Object>is(updatedOneMinuteRate));
        assertThat(extractMonitorables(meteredAdapter).get(MEAN_RATE).get(), Matchers.<Object>is(updatedMeanRate));
    }

    private Map<String, Monitorable> extractMonitorables(MetricAdapter timerAdapter) {
        Map<String, Monitorable> monitorables = Maps.newHashMap();
        for (Monitorable monitorable : timerAdapter.getMonitorables()) {
            final String name = monitorable.getName();
            monitorables.put(name.substring(name.lastIndexOf('.') + 1), monitorable);
        }
        return monitorables;
    }
}
