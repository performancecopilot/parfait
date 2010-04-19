package com.custardsource.parfait.timing;

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
        assertTrue(timing.ownTimeValue() >= 0);
        assertTrue(timing.totalValue() >= 0);
        assertTrue(timing.ownTimeValue() <= timing.totalValue());
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
        assertTrue("Metric value should end with correct unit name", timing.ownTimeValueFormatted()
                .endsWith(StandardThreadMetrics.CLOCK_TIME.getUnit().toString()));
    }
}
