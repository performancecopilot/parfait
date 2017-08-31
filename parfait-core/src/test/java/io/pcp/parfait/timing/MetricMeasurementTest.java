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

import junit.framework.TestCase;

public class MetricMeasurementTest extends TestCase {
    public void testCannotStartTwice() {
        MetricMeasurement timing = new MetricMeasurement(StandardThreadMetrics.CLOCK_TIME, Thread
                .currentThread());
        timing.startTimer();
        try {
            timing.startTimer();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotPauseBeforeStarted() {
        MetricMeasurement timing = new MetricMeasurement(StandardThreadMetrics.CLOCK_TIME, Thread
                .currentThread());
        try {
            timing.pauseOwnTime();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotResumeBeforeStarted() {
        MetricMeasurement timing = new MetricMeasurement(StandardThreadMetrics.CLOCK_TIME, Thread
                .currentThread());
        try {
            timing.resumeOwnTime();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotStopBeforeStarted() {
        MetricMeasurement timing = new MetricMeasurement(StandardThreadMetrics.CLOCK_TIME, Thread
                .currentThread());
        try {
            timing.stopTimer();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotPauseWhenPaused() {
        MetricMeasurement timing = new MetricMeasurement(StandardThreadMetrics.CLOCK_TIME, Thread
                .currentThread());
        timing.startTimer();
        timing.pauseOwnTime();
        try {
            timing.pauseOwnTime();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotPauseAfterStop() {
        MetricMeasurement timing = new MetricMeasurement(StandardThreadMetrics.CLOCK_TIME, Thread
                .currentThread());
        timing.startTimer();
        timing.stopTimer();
        try {
            timing.pauseOwnTime();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotResumeIfNotPaused() {
        MetricMeasurement timing = new MetricMeasurement(StandardThreadMetrics.CLOCK_TIME, Thread
                .currentThread());
        timing.startTimer();
        try {
            timing.resumeOwnTime();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotResumeAfterStop() {
        MetricMeasurement timing = new MetricMeasurement(StandardThreadMetrics.CLOCK_TIME, Thread
                .currentThread());
        timing.startTimer();
        timing.stopTimer();
        try {
            timing.resumeOwnTime();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotStopUnlessStarted() {
        MetricMeasurement timing = new MetricMeasurement(StandardThreadMetrics.CLOCK_TIME, Thread
                .currentThread());
        try {
            timing.stopTimer();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotAccessTotalTimeUntilStopped() {
        MetricMeasurement timing = new MetricMeasurement(StandardThreadMetrics.CLOCK_TIME, Thread
                .currentThread());
        timing.startTimer();
        try {
            timing.totalValue();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotAccessOwnTimeUntilStopped() {
        MetricMeasurement timing = new MetricMeasurement(StandardThreadMetrics.CLOCK_TIME, Thread
                .currentThread());
        timing.startTimer();
        try {
            timing.ownTimeValue();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testSanityOfNumbers() {
        // This test is unlikely to ever pick anything up but we may as well sanity check that the
        // numbers make sense (own time isn't > than total time)
        MetricMeasurement timing = new MetricMeasurement(StandardThreadMetrics.CLOCK_TIME, Thread
                .currentThread());
        timing.startTimer();
        timing.pauseOwnTime();
        timing.resumeOwnTime();
        timing.stopTimer();
        assertTrue(timing.ownTimeValue().getValue().longValue() >= 0);
        assertTrue(timing.totalValue().getValue().longValue() >= 0);
        assertTrue(timing.ownTimeValue().getValue().longValue() <= timing.totalValue().getValue().longValue());
    }

    public void testMetricName() {
        MetricMeasurement timing = new MetricMeasurement(StandardThreadMetrics.CLOCK_TIME, Thread
                .currentThread());
        assertEquals(StandardThreadMetrics.CLOCK_TIME.getMetricName(), timing.getMetricName());
    }

    public void testUnitDisplay() {
        MetricMeasurement timing = new MetricMeasurement(StandardThreadMetrics.CLOCK_TIME, Thread
                .currentThread());
        timing.startTimer();
        timing.stopTimer();
        assertTrue("Metric value should end with correct unit name", timing.ownTimeValue().toString()
                .endsWith(StandardThreadMetrics.CLOCK_TIME.getUnit().toString()));
    }
}
