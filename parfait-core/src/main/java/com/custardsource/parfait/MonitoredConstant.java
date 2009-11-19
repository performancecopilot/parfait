package com.custardsource.parfait;

import javax.measure.unit.Unit;

/**
 * MonitoredConstant provides an implementation of {@link Monitorable} for simple values that are
 * rarely (read: never) updated, once initialised.
 * <p>
 * This class should be used for PCP discrete values: a discrete value is a value that rarely
 * changes such as the number of CPUs or the application version number.
 * <p>
 * A setter exists for those cases where the value is not known until after creation, but this
 * should not be called as a matter of course.
 */
public class MonitoredConstant<T> extends SettableValue<T> {
    public MonitoredConstant(String name, String description, T initialValue) {
        this(name, description, MonitorableRegistry.DEFAULT_REGISTRY,
                initialValue);
    }

    public MonitoredConstant(String name, String description, T initialValue, Unit<?> unit) {
        this(name, description, MonitorableRegistry.DEFAULT_REGISTRY, initialValue, unit);
    }

    public MonitoredConstant(String name, String description, MonitorableRegistry registry,
            T initialValue) {
        this(name, description, registry, initialValue, Unit.ONE);
    }

    public MonitoredConstant(String name, String description, MonitorableRegistry registry,
            T initialValue, Unit<?> unit) {
        super(name, description, registry, initialValue, unit, ValueSemantics.CONSTANT);
    }
}