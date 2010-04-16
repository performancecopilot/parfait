package com.custardsource.parfait;

import javax.measure.unit.Unit;

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
     * Decrements the current value by 1.
     */
    public abstract void dec();
}
