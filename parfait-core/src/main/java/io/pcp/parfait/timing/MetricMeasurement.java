package io.pcp.parfait.timing;

import tec.uom.se.AbstractQuantity;
import tec.uom.se.quantity.NumberQuantity;

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
    private volatile Long startValue;
    private Long endValue;
    private Long lastStartOwnTimeValue;
    private long ownValueSoFar = 0L;
    private final ThreadMetric metricSource;
    private final Thread thread;

    public MetricMeasurement(ThreadMetric metricSource, Thread thread) {
        this.metricSource = metricSource;
        this.thread = thread;
    }

    public void startTimer() {
        Preconditions.checkState(startValue == null, "Can't start running timer");
        this.startValue = metricSource.getValueForThread(thread);
        this.lastStartOwnTimeValue = this.startValue;
        this.ownValueSoFar = 0L;
    }

    public void pauseOwnTime() {
    	Preconditions.checkState(startValue != null, "Can't pause own time while timer is stopped");
    	Preconditions.checkState(lastStartOwnTimeValue != null, "Can't pause own time while already paused");
        this.ownValueSoFar += (metricSource.getValueForThread(thread) - lastStartOwnTimeValue);
        this.lastStartOwnTimeValue = null;
    }

    public void resumeOwnTime() {
    	Preconditions.checkState(startValue != null, "Can't resume own time while timer is stopped");
    	Preconditions.checkState(lastStartOwnTimeValue == null, "Can't resume own time - already counting");
    	Preconditions.checkState(endValue == null, "Can't resume own time - stopped");
        this.lastStartOwnTimeValue = metricSource.getValueForThread(thread);
    }

    public void stopTimer() {
        pauseOwnTime();
        endValue = metricSource.getValueForThread(thread);
    }

    public AbstractQuantity<?> totalValue() {
    	Preconditions.checkState(endValue != null, "Can't measure time until timer is stopped");
        return NumberQuantity.of(endValue - startValue, metricSource.getUnit());
    }

    public AbstractQuantity<?> ownTimeValue() {
    	Preconditions.checkState(endValue != null, "Can't measure time until timer is stopped");
        return NumberQuantity.of(ownValueSoFar, metricSource.getUnit());
    }

    public long inProgressValue() {
        Long start = startValue;
        Long snapshot = metricSource.getValueForThread(thread);
        
        if (start == null || snapshot == null) {
            return 0;
        }
        return snapshot - start;
    }

    public String getMetricName() {
        return metricSource.getMetricName();
    }

    public ThreadMetric getMetricSource() {
        return metricSource;
    }
}
