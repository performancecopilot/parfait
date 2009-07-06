package com.custardsource.parfait.timing;

import com.google.common.base.Preconditions;

/**
 * Class to measure the time taken to run by a single event (or some similar 'delta' metric).
 * Can provide both 'forward' and 'backward' traces (that is, a list of events invoked by the
 * measured event, as well as the path taken to reach the event under timing) and provide
 * measures of the chosen metric for the 'total' and 'own' time spent in each (that is, e.g. how
 * long the execution of the event took from start to finish, and how long was actually spent
 * in this event's code as opposed to forwarding elsewhere).
 */
class MetricMeasurement {
    private Long startValue;
    private Long endValue;
    private Long lastStartOwnTimeValue;
    private long ownValueSoFar = 0L;
    private ThreadMetric metricSource;

    public MetricMeasurement(ThreadMetric metricSource) {
        this.metricSource = metricSource;
    }

    public void startTimer() {
        Preconditions.checkState(startValue == null, "Can't start running timer");
        this.startValue = metricSource.getCurrentValue();
        this.lastStartOwnTimeValue = metricSource.getCurrentValue();
        this.ownValueSoFar = 0L;
    }

    public void pauseOwnTime() {
    	Preconditions.checkState(startValue != null, "Can't pause own time while timer is stopped");
    	Preconditions.checkState(lastStartOwnTimeValue != null, "Can't pause own time while already paused");
        this.ownValueSoFar += (metricSource.getCurrentValue() - lastStartOwnTimeValue);
        this.lastStartOwnTimeValue = null;
    }

    public void resumeOwnTime() {
    	Preconditions.checkState(startValue != null, "Can't resume own time while timer is stopped");
    	Preconditions.checkState(lastStartOwnTimeValue == null, "Can't resume own time - already counting");
    	Preconditions.checkState(endValue == null, "Can't resume own time - stopped");
        this.lastStartOwnTimeValue = metricSource.getCurrentValue();
    }

    public void stopTimer() {
        pauseOwnTime();
        endValue = metricSource.getCurrentValue();
    }

    public long totalValue() {
    	Preconditions.checkState(endValue != null, "Can't measure time until timer is stopped");
        return endValue - startValue;
    }

    public long ownTimeValue() {
    	Preconditions.checkState(endValue != null, "Can't measure time until timer is stopped");
        return ownValueSoFar;
    }

    public String getMetricName() {
        return metricSource.getMetricName();
    }

    public String ownTimeValueFormatted() {
        return ownTimeValue() + metricSource.getUnit();
    }

    public String totalValueFormatted() {
        return totalValue() + metricSource.getUnit();
    }

    public ThreadMetric getMetricSource() {
        return metricSource;
    }
}