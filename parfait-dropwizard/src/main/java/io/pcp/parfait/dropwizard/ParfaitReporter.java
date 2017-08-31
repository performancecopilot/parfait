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

import static com.codahale.metrics.MetricRegistry.name;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import io.pcp.parfait.DynamicMonitoringView;
import io.pcp.parfait.Monitorable;
import io.pcp.parfait.MonitorableRegistry;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParfaitReporter extends ScheduledReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParfaitReporter.class);

    private static final String PARFAIT_REPORTER_NAME = "parfait-reporter";
    private final MonitorableRegistry monitorableRegistry;
    private final String metricNamePrefix;
    private final Map<String, MetricAdapter> publishedMetrics;
    private final MetricAdapterFactory metricAdapterFactory;

    private DynamicMonitoringView dynamicMonitoringView;

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public ParfaitReporter(MetricRegistry metricRegistry,
                           MonitorableRegistry monitorableRegistry,
                           MetricAdapterFactory metricAdapterFactory,
                           TimeUnit rateUnit,
                           TimeUnit durationUnit,
                           MetricFilter filter,
                           String metricNamePrefix) {
        super(metricRegistry, PARFAIT_REPORTER_NAME, filter, rateUnit, durationUnit);
        this.monitorableRegistry = monitorableRegistry;
        this.metricNamePrefix = metricNamePrefix;
        this.publishedMetrics = Maps.newHashMap();
        this.metricAdapterFactory = metricAdapterFactory;
    }

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public ParfaitReporter(MetricRegistry metricRegistry,
                           MonitorableRegistry monitorableRegistry,
                           DynamicMonitoringView dynamicMonitoringView,
                           MetricAdapterFactory metricAdapterFactory,
                           TimeUnit rateUnit,
                           TimeUnit durationUnit,
                           MetricFilter filter,
                           String prefix) {
        this(metricRegistry, monitorableRegistry, metricAdapterFactory, rateUnit, durationUnit, filter, prefix);
        this.dynamicMonitoringView = dynamicMonitoringView;
    }

    @SuppressWarnings("PMD.ExcessiveParameterList")
    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        try {
            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                publishMetric(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                publishMetric(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                publishMetric(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                publishMetric(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                publishMetric(entry.getKey(), entry.getValue());
            }
        } catch (RuntimeException ex) {
            LOGGER.error("An exception occurred publishing metrics to Parfait", ex);
        }
    }

    @Override
    public void start(long period, TimeUnit unit) {
        super.start(period, unit);
        if (dynamicMonitoringView != null) {
            dynamicMonitoringView.start();
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (dynamicMonitoringView != null) {
            dynamicMonitoringView.stop();
        }
    }

    private void publishMetric(String name, Metric metric) {
        getOrCreateMetricAdapter(name, metric).updateMonitorables();
    }

    private MetricAdapter getOrCreateMetricAdapter(String name, Metric metric) {
        MetricAdapter adapter;
        if (publishedMetrics.containsKey(name)) {
            adapter = publishedMetrics.get(name);
        } else {
            adapter = createAndRegisterMetricAdapter(name, metric);
        }
        return adapter;
    }

    private MetricAdapter createAndRegisterMetricAdapter(String name, Metric metric) {
        MetricAdapter adapter = metricAdapterFactory.createMetricAdapterFor(name(metricNamePrefix, name), metric);
        for (Monitorable monitorable : adapter.getMonitorables()) {
            monitorableRegistry.register(monitorable);
        }
        publishedMetrics.put(name, adapter);
        return adapter;
    }
}
