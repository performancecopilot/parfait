package com.custardsource.parfait.dropwizard.metricadapters;

import static tec.units.ri.unit.MetricPrefix.NANO;
import static tec.units.ri.unit.Units.SECOND;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import javax.measure.quantity.Time;
import javax.measure.Unit;
import java.util.Map;

import com.custardsource.parfait.dropwizard.MetricAdapter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.custardsource.parfait.Monitorable;
import com.google.common.collect.Maps;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TimerAdapterTest {

    public static final String NAME = "NAME";
    public static final String DESCRIPTION = "DESCRIPTION";

    private static final double INITIAL_FIFTEEN_MINUTE_RATE = 1.1;
    private static final double INITIAL_FIVE_MINUTE_RATE = 2.2;
    private static final double INITIAL_ONE_MINUTE_RATE = 3.3;
    private static final double INITIAL_MEAN_RATE = 4.4;

    private static final long INITIAL_COUNT = 5;

    private static final long INITIAL_MIN = 6;
    private static final long INITIAL_MAX = 7;
    private static final double INITIAL_MEAN = 8.8;
    private static final double INITIAL_MEDIAN = 9.9;
    private static final double INITIAL_STDDEV = 10.10;

    private static final Unit<Time> NANOSECOND = NANO(SECOND);
    private static final String FIFTEEN_MINUTE_RATE = "fifteen_minute_rate";
    private static final String FIVE_MINUTE_RATE = "five_minute_rate";
    private static final String ONE_MINUTE_RATE = "one_minute_rate";
    private static final String MEAN_RATE = "mean_rate";
    private static final String COUNT = "count";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String MEAN = "mean";
    private static final String MEDIAN = "median";
    private static final String STDDEV = "stddev";

    @Mock
    private Timer timer;
    
    @Mock
    private Snapshot snapshot;
    private TimerAdapter timerAdapter;

    @Before
    public void setUp() {
        when(timer.getSnapshot()).thenReturn(snapshot);

        when(snapshot.getMax()).thenReturn(INITIAL_MAX);
        when(snapshot.getMin()).thenReturn(INITIAL_MIN);
        when(snapshot.getMedian()).thenReturn(INITIAL_MEDIAN);
        when(snapshot.getMean()).thenReturn(INITIAL_MEAN);
        when(snapshot.getStdDev()).thenReturn(INITIAL_STDDEV);

        when(timer.getCount()).thenReturn(INITIAL_COUNT);

        when(timer.getFifteenMinuteRate()).thenReturn(INITIAL_FIFTEEN_MINUTE_RATE);
        when(timer.getFiveMinuteRate()).thenReturn(INITIAL_FIVE_MINUTE_RATE);
        when(timer.getOneMinuteRate()).thenReturn(INITIAL_ONE_MINUTE_RATE);
        when(timer.getMeanRate()).thenReturn(INITIAL_MEAN_RATE);

        timerAdapter = new TimerAdapter(timer, NAME, DESCRIPTION);
    }

    @Test
    public void shouldPublishFifteenMinuteRateMetric() {
        assertThat(extractMonitorables(timerAdapter).get(FIFTEEN_MINUTE_RATE), notNullValue());
        assertThat(extractMonitorables(timerAdapter).get(FIFTEEN_MINUTE_RATE).getDescription(), is(DESCRIPTION + " - Fifteen minute rate"));
        assertThat(extractMonitorables(timerAdapter).get(FIFTEEN_MINUTE_RATE).get(), Matchers.<Object>is(INITIAL_FIFTEEN_MINUTE_RATE));
    }

    @Test
    public void shouldPublishFiveMinuteRateMetric() {
        assertThat(extractMonitorables(timerAdapter).get(FIVE_MINUTE_RATE), notNullValue());
        assertThat(extractMonitorables(timerAdapter).get(FIVE_MINUTE_RATE).getDescription(), is(DESCRIPTION + " - Five minute rate"));
        assertThat(extractMonitorables(timerAdapter).get(FIVE_MINUTE_RATE).get(), Matchers.<Object>is(INITIAL_FIVE_MINUTE_RATE));
    }

    @Test
    public void shouldPublishOneMinuteRateMetric() {
        assertThat(extractMonitorables(timerAdapter).get(ONE_MINUTE_RATE), notNullValue());
        assertThat(extractMonitorables(timerAdapter).get(ONE_MINUTE_RATE).getDescription(), is(DESCRIPTION + " - One minute rate"));
        assertThat(extractMonitorables(timerAdapter).get(ONE_MINUTE_RATE).get(), Matchers.<Object>is(INITIAL_ONE_MINUTE_RATE));
    }

    @Test
    public void shouldPublishMeanRateMetric() {
        assertThat(extractMonitorables(timerAdapter).get(MEAN_RATE), notNullValue());
        assertThat(extractMonitorables(timerAdapter).get(MEAN_RATE).getDescription(), is(DESCRIPTION + " - Mean rate"));
        assertThat(extractMonitorables(timerAdapter).get(MEAN_RATE).get(), Matchers.<Object>is(INITIAL_MEAN_RATE));
    }

    @Test
    public void shouldPublishCountMetric() {
        assertThat(extractMonitorables(timerAdapter).get(COUNT), notNullValue());
        assertThat(extractMonitorables(timerAdapter).get(COUNT).getDescription(), is(DESCRIPTION + " - Count"));
        assertThat(extractMonitorables(timerAdapter).get(COUNT).get(), Matchers.<Object>is(INITIAL_COUNT));
    }

    @Test
    public void shouldPublishMinMetricInNanoseconds() {
        assertThat(extractMonitorables(timerAdapter).get(MIN), notNullValue());
        assertThat(extractMonitorables(timerAdapter).get(MIN).getDescription(), is(DESCRIPTION + " - Minimum"));
        assertThat(extractMonitorables(timerAdapter).get(MIN).get(), Matchers.<Object>is(INITIAL_MIN));
        assertThat(extractMonitorables(timerAdapter).get(MIN).getUnit(), Matchers.<Object>is(NANOSECOND));
    }

    @Test
    public void shouldPublishMaxMetricInNanoseconds() {
        assertThat(extractMonitorables(timerAdapter).get(MAX), notNullValue());
        assertThat(extractMonitorables(timerAdapter).get(MAX).getDescription(), is(DESCRIPTION + " - Maximum"));
        assertThat(extractMonitorables(timerAdapter).get(MAX).get(), Matchers.<Object>is(INITIAL_MAX));
        assertThat(extractMonitorables(timerAdapter).get(MAX).getUnit(), Matchers.<Object>is(NANOSECOND));


    }

    @Test
    public void shouldPublishMeanMetricInNanoseconds() {
        assertThat(extractMonitorables(timerAdapter).get(MEAN), notNullValue());
        assertThat(extractMonitorables(timerAdapter).get(MEAN).getDescription(), is(DESCRIPTION + " - Mean"));
        assertThat(extractMonitorables(timerAdapter).get(MEAN).get(), Matchers.<Object>is(INITIAL_MEAN));
        assertThat(extractMonitorables(timerAdapter).get(MEAN).getUnit(), Matchers.<Object>is(NANOSECOND));
    }

    @Test
    public void shouldPublishMedianMetricInNanoseconds() {
        assertThat(extractMonitorables(timerAdapter).get(MEDIAN), notNullValue());
        assertThat(extractMonitorables(timerAdapter).get(MEDIAN).getDescription(), is(DESCRIPTION + " - Median"));
        assertThat(extractMonitorables(timerAdapter).get(MEDIAN).get(), Matchers.<Object>is(INITIAL_MEDIAN));
        assertThat(extractMonitorables(timerAdapter).get(MEDIAN).getUnit(), Matchers.<Object>is(NANOSECOND));
    }

    @Test
    public void shouldPublishStdDevMetricInNanoseconds() {
        assertThat(extractMonitorables(timerAdapter).get(STDDEV), notNullValue());
        assertThat(extractMonitorables(timerAdapter).get(STDDEV).getDescription(), is(DESCRIPTION + " - Standard Deviation"));
        assertThat(extractMonitorables(timerAdapter).get(STDDEV).get(), Matchers.<Object>is(INITIAL_STDDEV));
        assertThat(extractMonitorables(timerAdapter).get(STDDEV).getUnit(), Matchers.<Object>is(NANOSECOND));
    }

    @Test
    public void shouldUpdateCountMetric() {
        long newCount = INITIAL_COUNT + 10;
        when(timer.getCount()).thenReturn(newCount);
        timerAdapter.updateMonitorables();
        assertThat(extractMonitorables(timerAdapter).get(COUNT).get(), Matchers.<Object>is(newCount));
    }

    @Test
    public void shouldUpdateMinMetric() {
        long newMin = INITIAL_MIN + 10;
        when(snapshot.getMin()).thenReturn(newMin);
        timerAdapter.updateMonitorables();
        assertThat(extractMonitorables(timerAdapter).get(MIN).get(), Matchers.<Object>is(newMin));
    }

    @Test
    public void shouldUpdateMaxMetric() {
        long newMax = INITIAL_MAX + 10;
        when(snapshot.getMax()).thenReturn(newMax);
        timerAdapter.updateMonitorables();
        assertThat(extractMonitorables(timerAdapter).get(MAX).get(), Matchers.<Object>is(newMax));
    }

    @Test
    public void shouldUpdateMeanMetric() {
        double newMean = INITIAL_MEAN + 10;
        when(snapshot.getMean()).thenReturn(newMean);
        timerAdapter.updateMonitorables();
        assertThat(extractMonitorables(timerAdapter).get(MEAN).get(), Matchers.<Object>is(newMean));
    }

    @Test
    public void shouldUpdateMedianMetric() {
        double newMedian = INITIAL_MEDIAN + 10;
        when(snapshot.getMedian()).thenReturn(newMedian);
        timerAdapter.updateMonitorables();
        assertThat(extractMonitorables(timerAdapter).get(MEDIAN).get(), Matchers.<Object>is(newMedian));
    }

    @Test
    public void shouldUpdateStdDevMetric() {
        double newStdDev = INITIAL_STDDEV + 10;
        when(snapshot.getStdDev()).thenReturn(newStdDev);
        timerAdapter.updateMonitorables();
        assertThat(extractMonitorables(timerAdapter).get(STDDEV).get(), Matchers.<Object>is(newStdDev));
    }

    @Test
    public void shouldUpdateFifteenMinuteRateMetric() {
        double newFifteenMinute = INITIAL_FIFTEEN_MINUTE_RATE + 10;
        when(timer.getFifteenMinuteRate()).thenReturn(newFifteenMinute);
        timerAdapter.updateMonitorables();
        assertThat(extractMonitorables(timerAdapter).get(FIFTEEN_MINUTE_RATE).get(), Matchers.<Object>is(newFifteenMinute));
    }

    @Test
    public void shouldUpdateFiveMinuteRateMetric() {
        double newFiveMinute = INITIAL_FIVE_MINUTE_RATE + 10;
        when(timer.getFiveMinuteRate()).thenReturn(newFiveMinute);
        timerAdapter.updateMonitorables();
        assertThat(extractMonitorables(timerAdapter).get(FIVE_MINUTE_RATE).get(), Matchers.<Object>is(newFiveMinute));
    }

    @Test
    public void shouldUpdateOneMinuteRateMetric() {
        double newOneMinute = INITIAL_ONE_MINUTE_RATE + 10;
        when(timer.getOneMinuteRate()).thenReturn(newOneMinute);
        timerAdapter.updateMonitorables();
        assertThat(extractMonitorables(timerAdapter).get(ONE_MINUTE_RATE).get(), Matchers.<Object>is(newOneMinute));
    }

    @Test
    public void shouldUpdateMeanRateMetric() {
        double newMeanRate = INITIAL_MEAN_RATE + 10;
        when(timer.getMeanRate()).thenReturn(newMeanRate);
        timerAdapter.updateMonitorables();
        assertThat(extractMonitorables(timerAdapter).get(MEAN_RATE).get(), Matchers.<Object>is(newMeanRate));
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
