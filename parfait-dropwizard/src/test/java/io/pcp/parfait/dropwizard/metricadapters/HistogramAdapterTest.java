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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;

import io.pcp.parfait.dropwizard.MetricAdapter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;
import io.pcp.parfait.Monitorable;
import com.google.common.collect.Maps;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HistogramAdapterTest {

    public static final String NAME = "NAME";
    public static final String DESCRIPTION = "DESCRIPTION";

    private static final long INITIAL_COUNT = 5;

    private static final long INITIAL_MIN = 6;
    private static final long INITIAL_MAX = 7;
    private static final double INITIAL_MEAN = 8.8;
    private static final double INITIAL_MEDIAN = 9.9;
    private static final double INITIAL_STDDEV = 10.10;
    private static final String COUNT = "count";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String MEAN = "mean";
    private static final String MEDIAN = "median";
    private static final String STDDEV = "stddev";

    @Mock
    private Histogram histogram;

    @Mock
    private Snapshot snapshot;
    private HistogramAdapter histogramAdapter;

    @Before
    public void setUp() {
        when(histogram.getSnapshot()).thenReturn(snapshot);

        when(snapshot.getMax()).thenReturn(INITIAL_MAX);
        when(snapshot.getMin()).thenReturn(INITIAL_MIN);
        when(snapshot.getMedian()).thenReturn(INITIAL_MEDIAN);
        when(snapshot.getMean()).thenReturn(INITIAL_MEAN);
        when(snapshot.getStdDev()).thenReturn(INITIAL_STDDEV);

        when(histogram.getCount()).thenReturn(INITIAL_COUNT);

        histogramAdapter = new HistogramAdapter(histogram, NAME, DESCRIPTION);
    }

    @Test
    public void shouldPublishCountMetric() {
        assertThat(extractMonitorables(histogramAdapter).get(COUNT), notNullValue());
        assertThat(extractMonitorables(histogramAdapter).get(COUNT).getDescription(), is(DESCRIPTION + " - Count"));
        assertThat(extractMonitorables(histogramAdapter).get(COUNT).get(), Matchers.<Object>is(INITIAL_COUNT));

    }

    @Test
    public void shouldPublishMinMetric() {
        assertThat(extractMonitorables(histogramAdapter).get(MIN), notNullValue());
        assertThat(extractMonitorables(histogramAdapter).get(MIN).getDescription(), is(DESCRIPTION + " - Minimum"));
        assertThat(extractMonitorables(histogramAdapter).get(MIN).get(), Matchers.<Object>is(INITIAL_MIN));

    }

    @Test
    public void shouldPublishMaxMetric() {
        assertThat(extractMonitorables(histogramAdapter).get(MAX), notNullValue());
        assertThat(extractMonitorables(histogramAdapter).get(MAX).getDescription(), is(DESCRIPTION + " - Maximum"));
        assertThat(extractMonitorables(histogramAdapter).get(MAX).get(), Matchers.<Object>is(INITIAL_MAX));

    }

    @Test
    public void shouldPublishMeanMetric() {
        assertThat(extractMonitorables(histogramAdapter).get(MEAN), notNullValue());
        assertThat(extractMonitorables(histogramAdapter).get(MEAN).getDescription(), is(DESCRIPTION + " - Mean"));
        assertThat(extractMonitorables(histogramAdapter).get(MEAN).get(), Matchers.<Object>is(INITIAL_MEAN));

    }

    @Test
    public void shouldPublishMedianMetric() {
        assertThat(extractMonitorables(histogramAdapter).get(MEDIAN), notNullValue());
        assertThat(extractMonitorables(histogramAdapter).get(MEDIAN).getDescription(), is(DESCRIPTION + " - Median"));
        assertThat(extractMonitorables(histogramAdapter).get(MEDIAN).get(), Matchers.<Object>is(INITIAL_MEDIAN));

    }

    @Test
    public void shouldPublishStdDevMetric() {
        assertThat(extractMonitorables(histogramAdapter).get(STDDEV), notNullValue());
        assertThat(extractMonitorables(histogramAdapter).get(STDDEV).getDescription(), is(DESCRIPTION + " - Standard Deviation"));
        assertThat(extractMonitorables(histogramAdapter).get(STDDEV).get(), Matchers.<Object>is(INITIAL_STDDEV));
    }

    @Test
    public void shouldUpdateCountMetric() {
        long newCount = INITIAL_COUNT + 10;
        when(histogram.getCount()).thenReturn(newCount);
        histogramAdapter.updateMonitorables();
        assertThat(extractMonitorables(histogramAdapter).get(COUNT).get(), Matchers.<Object>is(newCount));
    }

    @Test
    public void shouldUpdateMinMetric() {
        long newMin = INITIAL_MIN + 10;
        when(snapshot.getMin()).thenReturn(newMin);
        histogramAdapter.updateMonitorables();
        assertThat(extractMonitorables(histogramAdapter).get(MIN).get(), Matchers.<Object>is(newMin));
    }

    @Test
    public void shouldUpdateMaxMetric() {
        long newMax = INITIAL_MAX + 10;
        when(snapshot.getMax()).thenReturn(newMax);
        histogramAdapter.updateMonitorables();
        assertThat(extractMonitorables(histogramAdapter).get(MAX).get(), Matchers.<Object>is(newMax));
    }

    @Test
    public void shouldUpdateMeanMetric() {
        double newMean = INITIAL_MEAN + 10;
        when(snapshot.getMean()).thenReturn(newMean);
        histogramAdapter.updateMonitorables();
        assertThat(extractMonitorables(histogramAdapter).get(MEAN).get(), Matchers.<Object>is(newMean));
    }

    @Test
    public void shouldUpdateMedianMetric() {
        double newMedian = INITIAL_MEDIAN + 10;
        when(snapshot.getMedian()).thenReturn(newMedian);
        histogramAdapter.updateMonitorables();
        assertThat(extractMonitorables(histogramAdapter).get(MEDIAN).get(), Matchers.<Object>is(newMedian));
    }

    @Test
    public void shouldUpdateStdDevMetric() {
        double newStdDev = INITIAL_STDDEV + 10;
        when(snapshot.getStdDev()).thenReturn(newStdDev);
        histogramAdapter.updateMonitorables();
        assertThat(extractMonitorables(histogramAdapter).get(STDDEV).get(), Matchers.<Object>is(newStdDev));
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
