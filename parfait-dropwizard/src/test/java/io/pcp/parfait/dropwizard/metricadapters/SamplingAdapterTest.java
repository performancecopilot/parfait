package io.pcp.parfait.dropwizard.metricadapters;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import io.pcp.parfait.Monitorable;
import io.pcp.parfait.ValueSemantics;
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

    private static final long INITIAL_MAX = 11l;
    private static final long INITIAL_MIN = 22l;
    private static final Double INITIAL_MEDIAN = 12.34;
    private static final Double INITIAL_STDDEV = 56.78;
    private static final Double INITIAL_MEAN = 90.12;
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
}
