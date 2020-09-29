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

import static tech.units.indriya.AbstractUnit.ONE;
import static tech.units.indriya.unit.Units.GRAY;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.measure.Unit;

import io.pcp.parfait.dropwizard.metricadapters.CountingAdapter;
import io.pcp.parfait.dropwizard.metricadapters.GaugeAdapter;
import io.pcp.parfait.dropwizard.metricadapters.HistogramAdapter;
import io.pcp.parfait.dropwizard.metricadapters.MeteredAdapter;
import io.pcp.parfait.dropwizard.metricadapters.TimerAdapter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import io.pcp.parfait.Monitorable;
import io.pcp.parfait.ValueSemantics;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MetricAdapterFactoryImplTest {

    private static final int INITIAL_VALUE = 12345;
    private static final String NAME = "NAME";
    private static final String TRANSLATED_NAME ="TRANSLATED_NAME";
    private static final String DESCRIPTION = "some description";

    private MetricAdapterFactoryImpl metricAdapterFactory;

    @Mock
    private Counter counter;
    @Mock
    private Gauge gauge;
    @Mock
    private Meter meter;
    @Mock
    private Histogram histogram;
    @Mock
    private Timer timer;
    @Mock
    private Snapshot snapshot;
    @Mock
    private MetricDescriptorLookup metricDescriptorLookup;
    @Mock
    private MetricDescriptor mockDescriptor;
    @Mock
    private MetricNameTranslator metricNameTranslator;

    @Before
    public void setUp() {
        when(gauge.getValue()).thenReturn(INITIAL_VALUE);
        when(histogram.getSnapshot()).thenReturn(snapshot);
        when(timer.getSnapshot()).thenReturn(snapshot);
        metricAdapterFactory = new MetricAdapterFactoryImpl(metricDescriptorLookup, metricNameTranslator);

        when(mockDescriptor.getUnit()).thenReturn(GRAY);
        when(mockDescriptor.getDescription()).thenReturn(DESCRIPTION);
        when(mockDescriptor.getSemantics()).thenReturn(ValueSemantics.FREE_RUNNING);

        when(metricDescriptorLookup.getDescriptorFor(anyString())).thenReturn(mockDescriptor);
        when(metricNameTranslator.translate(NAME)).thenReturn(TRANSLATED_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenMetricIsAMetricSet() {
        metricAdapterFactory.createMetricAdapterFor(NAME, mock(MetricSet.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowUnsupportedOperationExceptionWhenMetricIsUnrecognized() {
        metricAdapterFactory.createMetricAdapterFor(NAME, mock(Metric.class));
    }

    @Test
    public void shouldNotConvertMetricNamesWhenNoConverterIsProvided() {
        metricAdapterFactory = new MetricAdapterFactoryImpl(metricDescriptorLookup);
        assertThat(getFirstMonitorable(metricAdapterFactory.createMetricAdapterFor(NAME, counter)).getName(), is(NAME));
    }

    @Test
    public void shouldUseTranslatedMetricNameToLookUpDescriptor() {
        metricAdapterFactory = new MetricAdapterFactoryImpl(metricDescriptorLookup, metricNameTranslator);
        metricAdapterFactory.createMetricAdapterFor(NAME, counter);
        verify(metricDescriptorLookup).getDescriptorFor(TRANSLATED_NAME);
    }

    //
    // Counter
    //

    @Test
    public void shouldProduceACountingAdapterWhenTheMetricIsACounter() {
        assertThat(metricAdapterFactory.createMetricAdapterFor(NAME, counter), is(instanceOf(CountingAdapter.class)));
    }

    @Test
    public void shouldUseTranslatedNameForNameOfReturnedCounterAdapter() {
        assertThat(getFirstMonitorable(metricAdapterFactory.createMetricAdapterFor(NAME, counter)).getName(), is(TRANSLATED_NAME));
    }

    @Test
    public void shouldUseRetrievedDescriptionForCounterAdapter() {
        assertThat(getFirstMonitorable(metricAdapterFactory.createMetricAdapterFor(NAME, counter)).getDescription(), startsWith(DESCRIPTION));
    }

    @Test
    public void shouldSpecifyOneAsUnitOfMeasurementInReturnedCounterAdapter() {
        assertThat(getFirstMonitorable(metricAdapterFactory.createMetricAdapterFor(NAME, counter)).getUnit(), Matchers.<Unit>is(ONE));
    }

    @Test
    public void shouldSpecifyFreeRunningAsValueSemanticsInReturnedCounterAdapter() {
        assertThat(getFirstMonitorable(metricAdapterFactory.createMetricAdapterFor(NAME, counter)).getSemantics(), is(ValueSemantics.FREE_RUNNING));
    }

    //
    // Gauge
    //

    @Test
    public void shouldProduceAGaugeAdapterWhenTheMetricIsAGauge() {
        assertThat(metricAdapterFactory.createMetricAdapterFor(NAME, gauge), is(instanceOf(GaugeAdapter.class)));
    }

    @Test
    public void shouldUseTranslatedNameForNameOfReturnedGaugeAdapter() {
        assertThat(getFirstMonitorable(metricAdapterFactory.createMetricAdapterFor(NAME, gauge)).getName(), is(TRANSLATED_NAME));
    }

    @Test
    public void shouldUseRetrievedDescriptionForGaugeAdapter() {
        assertThat(getFirstMonitorable(metricAdapterFactory.createMetricAdapterFor(NAME, gauge)).getDescription(), startsWith(DESCRIPTION));
    }

    @Test
    public void shouldSpecifyFreeRunningAsValueSemanticsInReturnedGaugeAdapter() {
        assertThat(getFirstMonitorable(metricAdapterFactory.createMetricAdapterFor(NAME, gauge)).getSemantics(), is(ValueSemantics.FREE_RUNNING));
    }

    @Test
    public void shouldUseUnitFromMetricDescriptorInGaugeAdapter() {
        assertThat(getFirstMonitorable(metricAdapterFactory.createMetricAdapterFor(NAME, gauge)).getUnit(), Matchers.<Unit>is(GRAY));
    }

    //
    // Histogram
    //

    @Test
    public void shouldProduceAHistogramAdapterWhenTheMetricIsAHistogram() {
        assertThat(metricAdapterFactory.createMetricAdapterFor(NAME, histogram), is(instanceOf(HistogramAdapter.class)));
    }

    @Test
    public void shouldUseTranslatedNameForNameOfReturnedHistogramAdapter() {
        assertThat(getFirstMonitorable(metricAdapterFactory.createMetricAdapterFor(NAME, histogram)).getName(), startsWith(TRANSLATED_NAME));
    }

    @Test
    public void shouldUseRetrievedDescriptionForHistogramAdapter() {
        assertThat(getFirstMonitorable(metricAdapterFactory.createMetricAdapterFor(NAME, histogram)).getDescription(), startsWith(DESCRIPTION));
    }

    @Test
    public void shouldUseUnitFromMetricDescriptorInHistogramAdapter() {
        assertThat(getMonitorableByName(TRANSLATED_NAME + ".mean", metricAdapterFactory.createMetricAdapterFor(NAME, histogram)).getUnit(), Matchers.<Unit>is(GRAY));
        assertThat(getMonitorableByName(TRANSLATED_NAME + ".median", metricAdapterFactory.createMetricAdapterFor(NAME, histogram)).getUnit(), Matchers.<Unit>is(GRAY));
        assertThat(getMonitorableByName(TRANSLATED_NAME + ".max", metricAdapterFactory.createMetricAdapterFor(NAME, histogram)).getUnit(), Matchers.<Unit>is(GRAY));
        assertThat(getMonitorableByName(TRANSLATED_NAME + ".min", metricAdapterFactory.createMetricAdapterFor(NAME, histogram)).getUnit(), Matchers.<Unit>is(GRAY));
        assertThat(getMonitorableByName(TRANSLATED_NAME + ".stddev", metricAdapterFactory.createMetricAdapterFor(NAME, histogram)).getUnit(), Matchers.<Unit>is(GRAY));
    }

    //
    // Timer
    //

    @Test
    public void shouldProduceATimerAdapterWhenTheMetricIsATimer() {
        assertThat(metricAdapterFactory.createMetricAdapterFor(NAME, timer), is(instanceOf(TimerAdapter.class)));
    }

    @Test
    public void shouldUseTranslatedNameForNameOfReturnedTimerAdapter() {
        assertThat(getFirstMonitorable(metricAdapterFactory.createMetricAdapterFor(NAME, timer)).getName(), startsWith(TRANSLATED_NAME));
    }

    @Test
    public void shouldUseRetrievedDescriptionForTimerAdapter() {
        assertThat(getFirstMonitorable(metricAdapterFactory.createMetricAdapterFor(NAME, timer)).getDescription(), startsWith(DESCRIPTION));
    }

    //
    // Meter
    //

    @Test
    public void shouldProduceAMeteredAdapter_WhenTheMetricIsAMeter() {
        assertThat(metricAdapterFactory.createMetricAdapterFor(NAME, meter), is(instanceOf(MeteredAdapter.class)));
    }

    @Test
    public void shouldUseTranslatedNameForNameOfReturnedMeteredAdapter() {
        assertThat(getFirstMonitorable(metricAdapterFactory.createMetricAdapterFor(NAME, meter)).getName(), startsWith(TRANSLATED_NAME));
    }

    @Test
    public void shouldUseRetrievedDescriptionForMeteredAdapter() {
        assertThat(getFirstMonitorable(metricAdapterFactory.createMetricAdapterFor(NAME, meter)).getDescription(), startsWith(DESCRIPTION));
    }

    private Monitorable getMonitorableByName(String name, MetricAdapter metricAdapter) {
        for (Monitorable monitorable : metricAdapter.getMonitorables()) {
            if (monitorable.getName().equals(name)) {
                return monitorable;
            }
        }
        throw new IllegalStateException("Couldn't locate metric :" + name);
    }

    private Monitorable getFirstMonitorable(MetricAdapter metricAdapter) {
        return metricAdapter.getMonitorables().iterator().next();
    }
}
