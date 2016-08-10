package com.custardsource.parfait.dropwizard.metricadapters;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.ValueSemantics;
import com.google.common.collect.Maps;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class SamplingAdapterTest {

    private static final String NAME = "NAME";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String MEAN = "mean";
    private static final String MEDIAN = "median";
    private static final String STDDEV = "stddev";
    private static final String SEVENTY_FIFTH = "seventyfifth";
    private static final String NINETY_FIFTH = "ninetyfifth";
    private static final String NINETY_EIGHTH = "ninetyeighth";
    private static final String NINETY_NINETH = "ninetynineth";
    private static final String THREE_NINES = "threenines";


    private static final long INITIAL_MAX = 11l;
    private static final long INITIAL_MIN = 22l;
    private static final Double INITIAL_MEDIAN = 12.34;
    private static final Double INITIAL_STDDEV = 56.78;
    private static final Double INITIAL_MEAN = 90.12;
    private static final Double INITIAL_75th = 75.75;
    private static final Double INITIAL_95th = 95.95;
    private static final Double INITIAL_98th = 98.98;
    private static final Double INITIAL_99th = 99.99;
    private static final Double INITIAL_THREE_NINES = 999.999;

    private SamplingAdapter adapter;

    @Mock
    private Sampling samplingMetric;
    @Mock
    private Snapshot snapshot;

    @Before
    public void setUp() {
        when(samplingMetric.getSnapshot()).thenReturn(snapshot);
        when(snapshot.getMax()).thenReturn(INITIAL_MAX);
        when(snapshot.getMin()).thenReturn(INITIAL_MIN);
        when(snapshot.getMedian()).thenReturn(INITIAL_MEDIAN);
        when(snapshot.getStdDev()).thenReturn(INITIAL_STDDEV);
        when(snapshot.getMean()).thenReturn(INITIAL_MEAN);

        when(snapshot.get75thPercentile()).thenReturn(INITIAL_75th);
        when(snapshot.get95thPercentile()).thenReturn(INITIAL_95th);
        when(snapshot.get98thPercentile()).thenReturn(INITIAL_98th);
        when(snapshot.get99thPercentile()).thenReturn(INITIAL_99th);
        when(snapshot.get999thPercentile()).thenReturn(INITIAL_THREE_NINES);

        adapter = new SamplingAdapter(samplingMetric, NAME, DESCRIPTION);
    }

    private Map<String, Monitorable> extractMonitorables(SamplingAdapter adapter) {
        Map<String, Monitorable> monitorables = Maps.newHashMap();
        for (Monitorable monitorable : adapter.getMonitorables()) {
            final String name = monitorable.getName();
            monitorables.put(name.substring(name.lastIndexOf('.') + 1), monitorable);
        }
        return monitorables;
    }

    @Test
    public void shouldPublishMinMetric() {
        final Monitorable<Long> minMonitorable = extractMonitorables(adapter).get(MIN);
        assertThat(minMonitorable, notNullValue());
        assertThat(minMonitorable.getDescription(), is(DESCRIPTION + " - Minimum"));
        assertThat(minMonitorable.getSemantics(), is(ValueSemantics.FREE_RUNNING));
        assertThat(minMonitorable.get(), is(INITIAL_MIN));
    }

    @Test
    public void shouldPublishMaxMetric() {
        final Monitorable<Long> maxMonitorable = extractMonitorables(adapter).get(MAX);
        assertThat(maxMonitorable, notNullValue());
        assertThat(maxMonitorable.getDescription(), is(DESCRIPTION + " - Maximum"));
        assertThat(maxMonitorable.getSemantics(), is(ValueSemantics.MONOTONICALLY_INCREASING));
        assertThat(maxMonitorable.get(), is(INITIAL_MAX));
    }

    @Test
    public void shouldPublishMeanMetric() {
        final Monitorable<Double> meanMonitorable = extractMonitorables(adapter).get(MEAN);
        assertThat(meanMonitorable, notNullValue());
        assertThat(meanMonitorable.getDescription(), is(DESCRIPTION + " - Mean"));
        assertThat(meanMonitorable.getSemantics(), is(ValueSemantics.FREE_RUNNING));
        assertThat(meanMonitorable.get(), is(INITIAL_MEAN));
    }

    @Test
    public void shouldPublishMedianMetric() {
        final Monitorable<Double> medianMonitorable = extractMonitorables(adapter).get(MEDIAN);
        assertThat(medianMonitorable, notNullValue());
        assertThat(medianMonitorable.getDescription(), is(DESCRIPTION + " - Median"));
        assertThat(medianMonitorable.getSemantics(), is(ValueSemantics.FREE_RUNNING));
        assertThat(medianMonitorable.get(), is(INITIAL_MEDIAN));
    }

    @Test
    public void shouldPublishStdDevMetric() {
        final Monitorable<Double> standardDevMetric = extractMonitorables(adapter).get(STDDEV);
        assertThat(standardDevMetric, notNullValue());
        assertThat(standardDevMetric.getDescription(), is(DESCRIPTION + " - Standard Deviation"));
        assertThat(standardDevMetric.getSemantics(), is(ValueSemantics.FREE_RUNNING));
        assertThat(standardDevMetric.get(), is(INITIAL_STDDEV));
    }

    @Test
    public void shouldPublish75thPercentileMetric() {
        final Monitorable<Double> seventyFifthMetric = extractMonitorables(adapter).get(SEVENTY_FIFTH);
        assertThat(seventyFifthMetric, notNullValue());
        assertThat(seventyFifthMetric.getDescription(), is(DESCRIPTION + " - 75th Percentile of recent data"));
        assertThat(seventyFifthMetric.getSemantics(), is(ValueSemantics.FREE_RUNNING));
        assertThat(seventyFifthMetric.get(), is(INITIAL_75th));
    }

    @Test
    public void shouldPublish95thPercentileMetric() {
        final Monitorable<Double> ninetyFifthMetric = extractMonitorables(adapter).get(NINETY_FIFTH);
        assertThat(ninetyFifthMetric, notNullValue());
        assertThat(ninetyFifthMetric.getDescription(), is(DESCRIPTION + " - 95th Percentile of recent data"));
        assertThat(ninetyFifthMetric.getSemantics(), is(ValueSemantics.FREE_RUNNING));
        assertThat(ninetyFifthMetric.get(), is(INITIAL_95th));
    }

    @Test
    public void shouldPublish98thPercentileMetric() {
        final Monitorable<Double> ninetyEigthMetric = extractMonitorables(adapter).get(NINETY_EIGHTH);
        assertThat(ninetyEigthMetric, notNullValue());
        assertThat(ninetyEigthMetric.getDescription(), is(DESCRIPTION + " - 98th Percentile of recent data"));
        assertThat(ninetyEigthMetric.getSemantics(), is(ValueSemantics.FREE_RUNNING));
        assertThat(ninetyEigthMetric.get(), is(INITIAL_98th));
    }

    @Test
    public void shouldPublish99thPercentileMetric() {
        final Monitorable<Double> ninetyNinthMetric = extractMonitorables(adapter).get(NINETY_NINETH);
        assertThat(ninetyNinthMetric, notNullValue());
        assertThat(ninetyNinthMetric.getDescription(), is(DESCRIPTION + " - 99th Percentile of recent data"));
        assertThat(ninetyNinthMetric.getSemantics(), is(ValueSemantics.FREE_RUNNING));
        assertThat(ninetyNinthMetric.get(), is(INITIAL_99th));
    }

    @Test
    public void shouldPublish999thPercentileMetric() {
        final Monitorable<Double> threeNinesMetric = extractMonitorables(adapter).get(THREE_NINES);
        assertThat(threeNinesMetric, notNullValue());
        assertThat(threeNinesMetric.getDescription(), is(DESCRIPTION + " - 99.9th Percentile of recent data"));
        assertThat(threeNinesMetric.getSemantics(), is(ValueSemantics.FREE_RUNNING));
        assertThat(threeNinesMetric.get(), is(INITIAL_THREE_NINES));
    }


    @Test
    public void shouldUpdateMinMetric() {
        long newMin = INITIAL_MIN - 5;
        when(snapshot.getMin()).thenReturn(newMin);
        adapter.updateMonitorables();
        assertThat(extractMonitorables(adapter).get(MIN).get(), Matchers.<Object>is(newMin));
    }

    @Test
    public void shouldUpdateMaxMetric() {
        long newMax = INITIAL_MAX + 5;
        when(snapshot.getMax()).thenReturn(newMax);
        adapter.updateMonitorables();
        assertThat(extractMonitorables(adapter).get(MAX).get(), Matchers.<Object>is(newMax));
    }

    @Test
    public void shouldUpdateMeanMetric() {
        double newMean = INITIAL_MEAN + 5;
        when(snapshot.getMean()).thenReturn(newMean);
        adapter.updateMonitorables();
        assertThat(extractMonitorables(adapter).get(MEAN).get(), Matchers.<Object>is(newMean));
    }

    @Test
    public void shouldUpdateMedianMetric() {
        double newMedian = INITIAL_MEDIAN + 5;
        when(snapshot.getMedian()).thenReturn(newMedian);
        adapter.updateMonitorables();
        assertThat(extractMonitorables(adapter).get(MEDIAN).get(), Matchers.<Object>is(newMedian));
    }

    @Test
    public void shouldUpdateStdDevMetric() {
        double newStdDev = INITIAL_STDDEV + 5;
        when(snapshot.getStdDev()).thenReturn(newStdDev);
        adapter.updateMonitorables();
        assertThat(extractMonitorables(adapter).get(STDDEV).get(), Matchers.<Object>is(newStdDev));
    }

    @Test
    public void shouldUpdate75thMetric() {
        double new75thMetric = INITIAL_75th + 5;
        when(snapshot.get75thPercentile()).thenReturn(new75thMetric);
        adapter.updateMonitorables();
        assertThat(extractMonitorables(adapter).get(SEVENTY_FIFTH).get(), Matchers.<Object>is(new75thMetric));
    }

    @Test
    public void shouldUpdate95thMetric() {
        double new95thMetric = INITIAL_95th + 5;
        when(snapshot.get95thPercentile()).thenReturn(new95thMetric);
        adapter.updateMonitorables();
        assertThat(extractMonitorables(adapter).get(NINETY_FIFTH).get(), Matchers.<Object>is(new95thMetric));
    }

    @Test
    public void shouldUpdate98thMetric() {
        double new98thMetric = INITIAL_98th + 5;
        when(snapshot.get98thPercentile()).thenReturn(new98thMetric);
        adapter.updateMonitorables();
        assertThat(extractMonitorables(adapter).get(NINETY_EIGHTH).get(), Matchers.<Object>is(new98thMetric));
    }

    @Test
    public void shouldUpdate99thMetric() {
        double new99thMetric = INITIAL_99th + 5;
        when(snapshot.get99thPercentile()).thenReturn(new99thMetric);
        adapter.updateMonitorables();
        assertThat(extractMonitorables(adapter).get(NINETY_NINETH).get(), Matchers.<Object>is(new99thMetric));
    }

    @Test
    public void shouldUpdate999thMetric() {
        double new999thMetric = INITIAL_THREE_NINES + 5;
        when(snapshot.get999thPercentile()).thenReturn(new999thMetric);
        adapter.updateMonitorables();
        assertThat(extractMonitorables(adapter).get(THREE_NINES).get(), Matchers.<Object>is(new999thMetric));
    }

}
