package com.custardsource.parfait.timing;

import com.aconex.utilities.Assert;

/**
 * Class to measure the time taken to run by a single controller (or some similar 'delta' metric).
 * Can provide both 'forward' and 'backward' traces (that is, a list of controllers invoked by the
 * measured controller, as well as the path taken to reach the controller under timing) and provide
 * measures of the chosen metric for the 'total' and 'own' time spent in each (that is, e.g. how
 * long the execution of the controller took from start to finish, and how long was actually spent
 * in this controller's code as opposed to forwarding elsewhere).
 */
class ControllerMetric {
    private Long startValue;
    private Long endValue;
    private Long lastStartOwnTimeValue;
    private long ownValueSoFar = 0L;
    private ThreadMetric metricSource;

    public ControllerMetric(ThreadMetric metricSource) {
        this.metricSource = metricSource;
    }

    public void startTimer() {
        Assert.isNull(startValue, "Can't start running timer");
        this.startValue = metricSource.getCurrentValue();
        this.lastStartOwnTimeValue = metricSource.getCurrentValue();
        this.ownValueSoFar = 0L;
    }

    public void pauseOwnTime() {
        Assert.notNull(startValue, "Can't pause own time while timer is stopped");
        Assert.notNull(lastStartOwnTimeValue, "Can't pause own time while already paused");
        this.ownValueSoFar += (metricSource.getCurrentValue() - lastStartOwnTimeValue);
        this.lastStartOwnTimeValue = null;
    }

    public void resumeOwnTime() {
        Assert.notNull(startValue, "Can't resume own time while timer is stopped");
        Assert.isNull(lastStartOwnTimeValue, "Can't resume own time - already counting");
        Assert.isNull(endValue, "Can't resume own time - stopped");
        this.lastStartOwnTimeValue = metricSource.getCurrentValue();
    }

    public void stopTimer() {
        pauseOwnTime();
        endValue = metricSource.getCurrentValue();
    }

    public long totalValue() {
        Assert.notNull(endValue, "Can't measure time until timer is stopped");
        return endValue - startValue;
    }

    public long ownTimeValue() {
        Assert.notNull(endValue, "Can't measure time until timer is stopped");
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