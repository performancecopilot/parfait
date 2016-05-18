package io.pcp.parfait;

import javax.measure.Unit;

/**
 * Base implementation of {@link Monitorable} which deals with numeric values.
 * Provides convenience methods to increment and decrement the current value by
 * one unit.
 */
abstract class MonitoredNumeric<T extends Number> extends MonitoredValue<T> {
    public MonitoredNumeric(String name, String description, MonitorableRegistry registry,
            T initialValue, Unit<?> unit) {
        super(name, description, registry, initialValue, unit);
    }

    /**
     * Increments the current value by 1.
     */
    public abstract void inc();

    /**
     * Increments the current value by the specified amount.
     */
    public abstract void inc(int delta);

    /**
     * Decrements the current value by 1.
     */
    public abstract void dec();

    /**
     * Decrements the current value by the specified amount.
     */
    public abstract void dec(int delta);
}
