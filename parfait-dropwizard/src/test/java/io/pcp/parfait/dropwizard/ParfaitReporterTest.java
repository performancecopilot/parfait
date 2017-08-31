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

package io.pcp.parfait.dropwizard;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.pcp.parfait.Monitorable;
import io.pcp.parfait.MonitorableRegistry;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParfaitReporterTest {

    private static final String TIMER_METRIC = "timerMetric";
    private static final String METER_METRIC = "meterMetric";
    private static final String HISTOGRAM_METRIC = "histogramMetric";
    private static final String COUNTER_METRIC = "counterMetric";
    private static final String GAUGE_METRIC = "gaugeMetric";
    private static final String METRIC_NAME_PREFIX = "bim.codahale";

    private ParfaitReporter parfaitReporter;
    @Mock
    private MetricFilter metricFilter;
    @Mock
    private TimeUnit durationUnit;
    @Mock
    private TimeUnit rateUnit;
    @Mock
    private MetricAdapterFactory metricAdapterFactory;
    @Mock
    private MetricAdapter metricAdapter;
    @Mock
    private Monitorable monitorable;
    @Mock
    private MonitorableRegistry monitorableRegistry;
    @Mock
    private MetricRegistry metricRegistry;
    @Mock
    private Gauge gauge;
    @Mock
    private Counter counter;
    @Mock
    private Histogram histogram;
    @Mock
    private Meter meter;
    @Mock
    private Timer timer;

    @Before
    public void setUp() {
        when(metricAdapter.getMonitorables()).thenReturn(Sets.newHashSet(monitorable));
        parfaitReporter = new ParfaitReporter(metricRegistry, monitorableRegistry, metricAdapterFactory, rateUnit, durationUnit, metricFilter, METRIC_NAME_PREFIX);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldAttemptToPublishAllMetricsDuringReport() {
        when(metricAdapterFactory.createMetricAdapterFor(anyString(), any(Metric.class))).thenReturn(metricAdapter);
        parfaitReporter.report(generateGauges(), generateCounters(), generateHistogram(), generateMeters(), generateTimers());
        verify(metricAdapterFactory).createMetricAdapterFor(METRIC_NAME_PREFIX + "." + TIMER_METRIC, timer);
        verify(metricAdapterFactory).createMetricAdapterFor(METRIC_NAME_PREFIX + "." + METER_METRIC, meter);
        verify(metricAdapterFactory).createMetricAdapterFor(METRIC_NAME_PREFIX + "." + HISTOGRAM_METRIC, histogram);
        verify(metricAdapterFactory).createMetricAdapterFor(METRIC_NAME_PREFIX + "." + COUNTER_METRIC, counter);
        verify(metricAdapterFactory).createMetricAdapterFor(METRIC_NAME_PREFIX + "." + GAUGE_METRIC, gauge);
        verify(monitorableRegistry, times(5)).register(monitorable);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldOnlyPublishOnlyPreviouslyUnseenMetricsDuringReport() {
        when(metricAdapterFactory.createMetricAdapterFor(anyString(), any(Metric.class))).thenReturn(metricAdapter);
        parfaitReporter.report(generateGauges(), generateCounters(), generateHistogram(), generateMeters(), generateTimers());
        parfaitReporter.report(generateGauges(), generateCounters(), generateHistogram(), generateMeters(), generateTimers());
        verify(monitorableRegistry, times(5)).register(any(Monitorable.class));

        // Add a new gauge for this report call
        SortedMap<String, Gauge> gauges = generateGauges();
        gauges.put("newGaugeMetric", mock(Gauge.class));
        parfaitReporter.report(gauges, generateCounters(), generateHistogram(), generateMeters(), generateTimers());
        verify(monitorableRegistry, times(6)).register(monitorable);
    }

    private SortedMap<String, Timer> generateTimers() {
        return newSortedMapContainingItem(TIMER_METRIC, timer);
    }

    private SortedMap<String, Meter> generateMeters() {
        return newSortedMapContainingItem(METER_METRIC, meter);
    }

    private SortedMap<String, Histogram> generateHistogram() {
        return newSortedMapContainingItem(HISTOGRAM_METRIC, histogram);
    }

    private SortedMap<String, Counter> generateCounters() {
        return newSortedMapContainingItem(COUNTER_METRIC, counter);
    }

    private SortedMap<String, Gauge> generateGauges() {
        return newSortedMapContainingItem(GAUGE_METRIC, gauge);
    }

    private <T> SortedMap<String, T> newSortedMapContainingItem(String key, T item) {
        SortedMap<String, T> newMap = Maps.newTreeMap();
        newMap.put(key, item);
        return newMap;
    }
}
