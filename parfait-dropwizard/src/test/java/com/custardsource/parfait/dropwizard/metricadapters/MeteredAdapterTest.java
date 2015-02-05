package com.custardsource.parfait.dropwizard.metricadapters;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.custardsource.parfait.dropwizard.MetricAdapter;
import com.codahale.metrics.Metered;
import com.custardsource.parfait.Monitorable;
import com.google.common.collect.Maps;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MeteredAdapterTest {

    public static final String NAME = "NAME";
    public static final String DESCRIPTION = "DESCRIPTION";

    private static final double INITIAL_FIFTEEN_MINUTE_RATE = 1.1;
    private static final double INITIAL_FIVE_MINUTE_RATE = 2.2;
    private static final double INITIAL_ONE_MINUTE_RATE = 3.3;
    private static final double INITIAL_MEAN_RATE = 4.4;
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

    private Map<String, Monitorable> extractMonitorables(MetricAdapter timerAdapter) {
        Map<String, Monitorable> monitorables = Maps.newHashMap();
        for (Monitorable monitorable : timerAdapter.getMonitorables()) {
            final String name = monitorable.getName();
            monitorables.put(name.substring(name.lastIndexOf('.') + 1), monitorable);
        }
        return monitorables;
    }
}
