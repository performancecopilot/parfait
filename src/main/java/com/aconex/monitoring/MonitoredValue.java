package com.aconex.monitoring;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.ObjectUtils;

import com.aconex.utilities.Assert;

/**
 * MonitoredValue provides a convenient implementation of {@link Monitorable} for basic values that
 * are updatable through a single set method call.
 * <p>
 * This class should be used for PCP instantaneous or discrete values.
 * <p>
 * An instantaneous value is a value that increments and decrements randomly over time. This is
 * essentially a "point in time" measurement. An example of an instantaneous value is the number of
 * active HTTP sessions.
 * <p>
 * A discrete value is a value that rarely changes such as the number of CPUs or the application
 * version number.
 * <p>
 * It is recommended that counters be implemented using the class {@link MonitoredCounter}.
 * 
 * @author ohutchison
 */
public class MonitoredValue<T> extends AbstractMonitorable<T> {

    private volatile T value;

    public MonitoredValue(String name, String description, T initialValue) {
        super(name, description, (Class<T>) initialValue.getClass());
        Assert.notNull(initialValue, "Monitored value can not be null");
        this.value = initialValue;
        registerSelf();
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        if (ObjectUtils.equals(this.value, newValue)) {
            return;
        }
        Assert.notNull(newValue, "Monitored value can not be null");
        this.value = newValue;
        notifyMonitors();
    }

    /**
     * Convenience method to increment atomic numeric types. Note that this MonitoredValue must be
     * of type AtomicInteger or AtomicLong for this method to work.
     */
    public void inc() {
        Assert.isTrue(value instanceof AtomicInteger || value instanceof AtomicLong,
                "MonitoredValue type has no increment capability");
        if (value instanceof AtomicInteger) {
            ((AtomicInteger) value).incrementAndGet();
        } else if (value instanceof AtomicLong) {
            ((AtomicLong) value).incrementAndGet();
        }
        notifyMonitors();
    }

    /**
     * Convenience method to decrement atomic numeric types. Note that this MonitoredValue must be
     * of type AtomicInteger or AtomicLong for this method to work.
     */
    public void dec() {
        Assert.isTrue(value instanceof AtomicInteger || value instanceof AtomicLong,
                "MonitoredValue type has no decrement capability");
        if (value instanceof AtomicInteger) {
            ((AtomicInteger) value).decrementAndGet();
        } else if (value instanceof AtomicLong) {
            ((AtomicLong) value).decrementAndGet();
        }
        notifyMonitors();
    }

    protected void logValue() {
        if (LOG.isTraceEnabled()) {
            LOG.trace(getName() + "=" + value);
        }
    }
}