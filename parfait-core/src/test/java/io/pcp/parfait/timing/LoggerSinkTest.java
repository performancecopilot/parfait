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

package io.pcp.parfait.timing;

import static tech.units.indriya.unit.Units.HERTZ;
import static javax.measure.MetricPrefix.MEGA;

import javax.measure.Unit;
import junit.framework.TestCase;

public class LoggerSinkTest extends TestCase {
    public void testShouldProduceExpectedMetricString() {
        MetricMeasurement measurement = getUnitMeasurement(HERTZ, 1);
        String result = new LoggerSink().buildSingleMetricResult(measurement);
        assertEquals("dummy: own 1 Hz, total 1 Hz", result);
    }

    public void testShouldNormalizeMetricStringToCorrectUnit() {
        MetricMeasurement measurement = getUnitMeasurement(HERTZ, 1000000001);
        LoggerSink sink = new LoggerSink();
        sink.normalizeUnits(HERTZ, MEGA(HERTZ));
        String result = sink.buildSingleMetricResult(measurement);
        assertEquals("dummy: own 1000.000001 MHz, total 1000.000001 MHz", result);
    }

    private MetricMeasurement getUnitMeasurement(Unit<?> unit, int amount) {
        DummyThreadMetric metric = new DummyThreadMetric(unit);
        MetricMeasurement measurement = new MetricMeasurement(metric, Thread.currentThread());
        measurement.startTimer();
        metric.incrementValue(amount);
        measurement.stopTimer();
        return measurement;
    }

}
