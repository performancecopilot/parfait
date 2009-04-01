package com.aconex.monitoring;

import java.util.concurrent.atomic.AtomicLong;

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
 * 
 * @author ohutchison
 */
public class MonitoredCounter extends AbstractMonitorable<Long> {

    private final AtomicLong value;

    public MonitoredCounter(String name, String description) {
    	this(name, description, MonitorableRegistry.DEFAULT_REGISTRY);
    }

    public MonitoredCounter(String name, String description, MonitorableRegistry registry) {
        super(name, description, Long.class);
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
