package com.custardsource.parfait;

import java.util.concurrent.atomic.AtomicLong;

import javax.measure.unit.Unit;

/**
 * A MonitoredCounter is a useful implementation of {@link Monitorable} specifically for
 * implementing long counters.
 * <p>
 * This class should be used to measure incrementing counter values only. For any other values, use
 * {@link MonitoredValue}.
 * <p>
 * A PCP counter is a value that increments over time due to an event. An example of a counter is
 * the number of JMS messages sent. Note that this class explicitly provides an atomic increment
 * operation only. Decrement and set methods should not be added.
 * <p>
 */
public class MonitoredCounter extends AbstractMonitorable<Long> {

    private final AtomicLong value;

    public MonitoredCounter(String name, String description) {
    	this(name, description, MonitorableRegistry.DEFAULT_REGISTRY);
    }

    public MonitoredCounter(String name, String description, MonitorableRegistry registry) {
        this(name, description, registry, Unit.ONE);
    }

    public MonitoredCounter(String name, String description, Unit<?> unit) {
        this(name, description, MonitorableRegistry.DEFAULT_REGISTRY, unit);
    }

    public MonitoredCounter(String name, String description, MonitorableRegistry registry,
            Unit<?> unit) {
        super(name, description, Long.class, unit, ValueSemantics.MONOTONICALLY_INCREASING);
        value = new AtomicLong(0L);
        registerSelf(registry);
    }

    public Long get() {
        return value.get();
    }

    /**
     * Increments the counter by a given value.
     * 
     * @param value
     *            the amount to increment
     */
    public void inc(long value) {
        this.value.addAndGet(value);
        notifyMonitors();
    }

    /**
     * Increments the counter by one.
     */
    public void inc() {
        value.incrementAndGet();
        notifyMonitors();
    }

    protected void logValue() {
        if (LOG.isTraceEnabled()) {
            LOG.trace(getName() + "=" + get());
        }
    }
}
