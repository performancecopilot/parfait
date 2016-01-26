package com.custardsource.parfait.dropwizard.metricadapters;

import com.codahale.metrics.Metered;
import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.ValueSemantics;
import com.custardsource.parfait.dropwizard.MetricAdapter;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeteredAdapterTest {

    public static final String NAME = "NAME";
    public static final String DESCRIPTION = "DESCRIPTION";

    private static final double INITIAL_FIFTEEN_MINUTE_RATE = 1.1;
    private static final double INITIAL_FIVE_MINUTE_RATE = 2.2;
    private static final double INITIAL_ONE_MINUTE_RATE = 3.3;
    private static final double INITIAL_MEAN_RATE = 4.4;
    private static final long INITIAL_RAW_COUNTER_VALUE = 42;
    private static final String FIFTEEN_MINUTE_RATE = "fifteen_minute_rate";
    private static final String FIVE_MINUTE_RATE = "five_minute_rate";
    private static final String ONE_MINUTE_RATE = "one_minute_rate";
    private static final String MEAN_RATE = "mean_rate";

    @Mock
    private Metered metered;

    private MeteredAdapter meteredAdapter;

    @Before
    public void setUp() {
        when(metered.getFifteenMinuteRate()).thenReturn(INITIAL_FIFTEEN_MINUTE_RATE);
        when(metered.getFiveMinuteRate()).thenReturn(INITIAL_FIVE_MINUTE_RATE);
        when(metered.getOneMinuteRate()).thenReturn(INITIAL_ONE_MINUTE_RATE);
        when(metered.getMeanRate()).thenReturn(INITIAL_MEAN_RATE);
        when(metered.getCount()).thenReturn(INITIAL_RAW_COUNTER_VALUE);

        meteredAdapter = new MeteredAdapter(metered, NAME, DESCRIPTION);
    }

    @Test
    public void shouldPublishFifteenMinuteRateMetricInNanoseconds() {
        assertThat(extractMonitorables(meteredAdapter).get(FIFTEEN_MINUTE_RATE), notNullValue());
        assertThat(extractMonitorables(meteredAdapter).get(FIFTEEN_MINUTE_RATE).getDescription(), is(DESCRIPTION + " - Fifteen minute rate"));
        assertThat(extractMonitorables(meteredAdapter).get(FIFTEEN_MINUTE_RATE).get(), Matchers.<Object>is(INITIAL_FIFTEEN_MINUTE_RATE));
    }

    @Test
    public void shouldPublishFiveMinuteRateMetric() {
        assertThat(extractMonitorables(meteredAdapter).get(FIVE_MINUTE_RATE), notNullValue());
        assertThat(extractMonitorables(meteredAdapter).get(FIVE_MINUTE_RATE).getDescription(), is(DESCRIPTION + " - Five minute rate"));
        assertThat(extractMonitorables(meteredAdapter).get(FIVE_MINUTE_RATE).get(), Matchers.<Object>is(INITIAL_FIVE_MINUTE_RATE));
    }

    @Test
    public void shouldPublishOneMinuteRateMetric() {
        assertThat(extractMonitorables(meteredAdapter).get(ONE_MINUTE_RATE), notNullValue());
        assertThat(extractMonitorables(meteredAdapter).get(ONE_MINUTE_RATE).getDescription(), is(DESCRIPTION + " - One minute rate"));
        assertThat(extractMonitorables(meteredAdapter).get(ONE_MINUTE_RATE).get(), Matchers.<Object>is(INITIAL_ONE_MINUTE_RATE));
    }

    @Test
    public void shouldPublishMeanRateMetric() {
        assertThat(extractMonitorables(meteredAdapter).get(MEAN_RATE), notNullValue());
        assertThat(extractMonitorables(meteredAdapter).get(MEAN_RATE).getDescription(), is(DESCRIPTION + " - Mean rate"));
        assertThat(extractMonitorables(meteredAdapter).get(MEAN_RATE).get(), Matchers.<Object>is(INITIAL_MEAN_RATE));
    }

    @Test
    public void shouldPublishRawCounterMetric() {
        Monitorable baseRawCounter = locateRawCounter();
        assertNotNull("There should have been a Monitorable with the base name", baseRawCounter);

        assertEquals("The base raw counter should be a PCP counter (Monotonically Increasing)", baseRawCounter.getSemantics(), ValueSemantics.MONOTONICALLY_INCREASING);

        assertThat(baseRawCounter.get(), Matchers.<Object>is(INITIAL_RAW_COUNTER_VALUE));
    }

    private Monitorable locateRawCounter() {
        Predicate<Monitorable> baseRawCounterPredicate = new Predicate<Monitorable>() {
            @Override
            public boolean apply(Monitorable monitorable) {
                return monitorable.getName().equals(NAME);
            }
        };

        assertTrue("There should have been a Monitorable with the base name", Iterables.any(meteredAdapter.getMonitorables(), baseRawCounterPredicate));
        return Iterables.find(meteredAdapter.getMonitorables(), baseRawCounterPredicate);
    }

    @Test
    public void shouldUpdateValues() {
        when(metered.getMeanRate()).thenReturn(43.0);
        when(metered.getCount()).thenReturn(43L);

        Monitorable rawCounter = locateRawCounter();

        meteredAdapter.updateMonitorables();

        assertThat("The Mean value has not been updated since the initial value was set", extractMonitorables(meteredAdapter).get(MEAN_RATE).get(), Matchers.<Object>is(43.0));
        assertThat("The raw counter value has not been updated since the initial value was set", rawCounter.get(), Matchers.<Object>is(43L));
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
