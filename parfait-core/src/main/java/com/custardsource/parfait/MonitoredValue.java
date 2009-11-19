package com.custardsource.parfait;

import javax.measure.unit.Unit;

/**
 * MonitoredValue provides a convenient implementation of {@link Monitorable} for basic values that
 * are updatable through a single set method call.
 * <p>
 * <p>
 * An instantaneous value is a value that increments and decrements randomly over time. This is
 * essentially a "point in time" measurement. An example of an instantaneous value is the number of
 * active HTTP sessions.
 * <p>
 * It is recommended that counters be implemented using the class {@link MonitoredCounter}.
 */
public class MonitoredValue<T> extends SettableValue<T> {
    public MonitoredValue(String name, String description, T initialValue) {
        this(name, description, MonitorableRegistry.DEFAULT_REGISTRY,
                initialValue);
    }

    public MonitoredValue(String name, String description, T initialValue, Unit<?> unit) {
        this(name, description, MonitorableRegistry.DEFAULT_REGISTRY, initialValue, unit);
    }

    public MonitoredValue(String name, String description, MonitorableRegistry registry,
            T initialValue) {
        this(name, description, registry, initialValue, Unit.ONE);
    }

    public MonitoredValue(String name, String description, MonitorableRegistry registry,
            T initialValue, Unit<?> unit) {
        super(name, description, registry, initialValue, unit, ValueSemantics.FREE_RUNNING);
    }
}