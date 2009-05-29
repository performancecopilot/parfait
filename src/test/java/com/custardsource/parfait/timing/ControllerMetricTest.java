package com.custardsource.parfait.timing;

import junit.framework.TestCase;

import com.aconex.utilities.AssertionFailedException;

public class ControllerMetricTest extends TestCase {
    public void testCannotStartTwice() {
        ControllerMetric timing = new ControllerMetric(MetricSources.CLOCK_TIME_METRIC_SOURCE);
        timing.startTimer();
        try {
            timing.startTimer();
            fail();
        } catch (AssertionFailedException e) {
            // Expected
        }
    }

    public void testCannotPauseBeforeStarted() {
        ControllerMetric timing = new ControllerMetric(MetricSources.CLOCK_TIME_METRIC_SOURCE);
        try {
            timing.pauseOwnTime();
            fail();
        } catch (AssertionFailedException e) {
            // Expected
        }
    }

    public void testCannotResumeBeforeStarted() {
        ControllerMetric timing = new ControllerMetric(MetricSources.CLOCK_TIME_METRIC_SOURCE);
        try {
            timing.resumeOwnTime();
            fail();
        } catch (AssertionFailedException e) {
            // Expected
        }
    }

    public void testCannotStopBeforeStarted() {
        ControllerMetric timing = new ControllerMetric(MetricSources.CLOCK_TIME_METRIC_SOURCE);
        try {
            timing.stopTimer();
            fail();
        } catch (AssertionFailedException e) {
            // Expected
        }
    }

    public void testCannotPauseWhenPaused() {
        ControllerMetric timing = new ControllerMetric(MetricSources.CLOCK_TIME_METRIC_SOURCE);
        timing.startTimer();
        timing.pauseOwnTime();
        try {
            timing.pauseOwnTime();
            fail();
        } catch (AssertionFailedException e) {
            // Expected
        }
    }

    public void testCannotPauseAfterStop() {
        ControllerMetric timing = new ControllerMetric(MetricSources.CLOCK_TIME_METRIC_SOURCE);
        timing.startTimer();
        timing.stopTimer();
        try {
            timing.pauseOwnTime();
            fail();
        } catch (AssertionFailedException e) {
            // Expected
        }
    }

    public void testCannotResumeIfNotPaused() {
        ControllerMetric timing = new ControllerMetric(MetricSources.CLOCK_TIME_METRIC_SOURCE);
        timing.startTimer();
        try {
            timing.resumeOwnTime();
            fail();
        } catch (AssertionFailedException e) {
            // Expected
        }
    }

    public void testCannotResumeAfterStop() {
        ControllerMetric timing = new ControllerMetric(MetricSources.CLOCK_TIME_METRIC_SOURCE);
        timing.startTimer();
        timing.stopTimer();
        try {
            timing.resumeOwnTime();
            fail();
        } catch (AssertionFailedException e) {
            // Expected
        }
    }

    public void testCannotStopUnlessStarted() {
        ControllerMetric timing = new ControllerMetric(MetricSources.CLOCK_TIME_METRIC_SOURCE);
        try {
            timing.stopTimer();
            fail();
        } catch (AssertionFailedException e) {
            // Expected
        }
    }

    public void testCannotAccessTotalTimeUntilStopped() {
        ControllerMetric timing = new ControllerMetric(MetricSources.CLOCK_TIME_METRIC_SOURCE);
        timing.startTimer();
        try {
            timing.totalValue();
            fail();
        } catch (AssertionFailedException e) {
            // Expected
        }
    }

    public void testCannotAccessOwnTimeUntilStopped() {
        ControllerMetric timing = new ControllerMetric(MetricSources.CLOCK_TIME_METRIC_SOURCE);
        timing.startTimer();
        try {
            timing.ownTimeValue();
            fail();
        } catch (AssertionFailedException e) {
            // Expected
        }
    }

    public void testSanityOfNumbers() {
        // This test is unlikely to ever pick anything up but we may as well sanity check that the
        // numbers make sense (own time isn't > than total time)
        ControllerMetric timing = new ControllerMetric(MetricSources.CLOCK_TIME_METRIC_SOURCE);
        timing.startTimer();
        timing.pauseOwnTime();
        timing.resumeOwnTime();
        timing.stopTimer();
        assertTrue(timing.ownTimeValue() >= 0);
        assertTrue(timing.totalValue() >= 0);
        assertTrue(timing.ownTimeValue() <= timing.totalValue());
    }

    public void testMetricName() {
        ControllerMetric timing = new ControllerMetric(MetricSources.CLOCK_TIME_METRIC_SOURCE);
        assertEquals(MetricSources.CLOCK_TIME_METRIC_SOURCE.getMetricName(), timing.getMetricName());
    }

    public void testUnitDisplay() {
        ControllerMetric timing = new ControllerMetric(MetricSources.CLOCK_TIME_METRIC_SOURCE);
        timing.startTimer();
        timing.stopTimer();
        assertTrue("Metric value should end with correct unit name", timing.ownTimeValueFormatted()
                .endsWith(MetricSources.CLOCK_TIME_METRIC_SOURCE.getUnit()));
    }
}
