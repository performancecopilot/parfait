package com.custardsource.parfait.timing;

import junit.framework.TestCase;

public class ControllerMetricTest extends TestCase {
    public void testCannotStartTwice() {
        ControllerMetric timing = new ControllerMetric(StandardThreadMetrics.CLOCK_TIME);
        timing.startTimer();
        try {
            timing.startTimer();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotPauseBeforeStarted() {
        ControllerMetric timing = new ControllerMetric(StandardThreadMetrics.CLOCK_TIME);
        try {
            timing.pauseOwnTime();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotResumeBeforeStarted() {
        ControllerMetric timing = new ControllerMetric(StandardThreadMetrics.CLOCK_TIME);
        try {
            timing.resumeOwnTime();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotStopBeforeStarted() {
        ControllerMetric timing = new ControllerMetric(StandardThreadMetrics.CLOCK_TIME);
        try {
            timing.stopTimer();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotPauseWhenPaused() {
        ControllerMetric timing = new ControllerMetric(StandardThreadMetrics.CLOCK_TIME);
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
        ControllerMetric timing = new ControllerMetric(StandardThreadMetrics.CLOCK_TIME);
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
        ControllerMetric timing = new ControllerMetric(StandardThreadMetrics.CLOCK_TIME);
        timing.startTimer();
        try {
            timing.resumeOwnTime();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotResumeAfterStop() {
        ControllerMetric timing = new ControllerMetric(StandardThreadMetrics.CLOCK_TIME);
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
        ControllerMetric timing = new ControllerMetric(StandardThreadMetrics.CLOCK_TIME);
        try {
            timing.stopTimer();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotAccessTotalTimeUntilStopped() {
        ControllerMetric timing = new ControllerMetric(StandardThreadMetrics.CLOCK_TIME);
        timing.startTimer();
        try {
            timing.totalValue();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCannotAccessOwnTimeUntilStopped() {
        ControllerMetric timing = new ControllerMetric(StandardThreadMetrics.CLOCK_TIME);
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
        ControllerMetric timing = new ControllerMetric(StandardThreadMetrics.CLOCK_TIME);
        timing.startTimer();
        timing.pauseOwnTime();
        timing.resumeOwnTime();
        timing.stopTimer();
        assertTrue(timing.ownTimeValue() >= 0);
        assertTrue(timing.totalValue() >= 0);
        assertTrue(timing.ownTimeValue() <= timing.totalValue());
    }

    public void testMetricName() {
        ControllerMetric timing = new ControllerMetric(StandardThreadMetrics.CLOCK_TIME);
        assertEquals(StandardThreadMetrics.CLOCK_TIME.getMetricName(), timing.getMetricName());
    }

    public void testUnitDisplay() {
        ControllerMetric timing = new ControllerMetric(StandardThreadMetrics.CLOCK_TIME);
        timing.startTimer();
        timing.stopTimer();
        assertTrue("Metric value should end with correct unit name", timing.ownTimeValueFormatted()
                .endsWith(StandardThreadMetrics.CLOCK_TIME.getUnit()));
    }
}
