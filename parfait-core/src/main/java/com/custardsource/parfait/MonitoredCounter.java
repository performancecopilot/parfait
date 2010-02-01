package com.custardsource.parfait;

import java.util.concurrent.atomic.AtomicLong;

import javax.measure.unit.Unit;

/**
 * A MonitoredCounter is a useful implementation of {@link Monitorable} specifically for
 * implementing long-valued counters.
 * <p>
 * This class should be used to measure incrementing counter values only. For any other values, use
 * {@link MonitoredValue} or another subclass of {@link SettableValue}.
 * <p>
 * In Parfait terms, a counter is a value that increments over time due to an event. An example of a counter might
 * the number of JMS messages sent or Garbage collections completed. Note that this class explicitly provides an atomic increment
 * operation only. Values must not decrement or be set to an arbitrary value.
 * <p>
 */
public class MonitoredCounter extends AbstractMonitorable<Long> {
    private final AtomicLong value = new AtomicLong(0L);

    /**
     * Creates a new MonitoredCounter against
     * {@link MonitorableRegistry#DEFAULT_REGISTRY the default registry} with no
     * unit semantics.
     */
    public MonitoredCounter(String name, String description) {
    	this(name, description, MonitorableRegistry.DEFAULT_REGISTRY);
    }

    /**
     * Creates a new MonitoredCounter against the given registry with no unit
     * semantics.
     */
    public MonitoredCounter(String name, String description, MonitorableRegistry registry) {
        this(name, description, registry, Unit.ONE);
    }

    /**
     * Creates a new MonitoredCounter against
     * {@link MonitorableRegistry#DEFAULT_REGISTRY the default registry}
     */
    public MonitoredCounter(String name, String description, Unit<?> unit) {
        this(name, description, MonitorableRegistry.DEFAULT_REGISTRY, unit);
    }

    /**
     * Creates a new MonitoredCounter against the provided
     * {@link MonitorableRegistry} with the given unit semantics.
     */
    public MonitoredCounter(String name, String description, MonitorableRegistry registry,
            Unit<?> unit) {
        super(name, description, Long.class, unit, ValueSemantics.MONOTONICALLY_INCREASING);
        registerSelf(registry);
    }

    @Override
    public Long get() {
        return value.get();
    }

    /**
     * Increments the counter by a given value.
     * 
     * @param value
     *            the amount to increment. Should be non-negative
     */
    public void inc(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Cannot increment counter " + getName()
                    + " by negative value " + value);
        }
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
}
