package com.custardsource.parfait;

import javax.measure.unit.Unit;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.base.Preconditions;

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
 */
public class MonitoredValue<T> extends AbstractMonitorable<T> {

    protected volatile T value;

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

    @SuppressWarnings("unchecked")
    public MonitoredValue(String name, String description, MonitorableRegistry registry,
            T initialValue, Unit<?> unit) {
        super(name, description, (Class<T>) initialValue.getClass(), unit);
        Preconditions.checkNotNull(initialValue, "Monitored value can not be null");
        this.value = initialValue;
        registerSelf(registry);
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        if (ObjectUtils.equals(this.value, newValue)) {
            return;
        }
        Preconditions.checkNotNull(newValue, "Monitored value can not be null");
        this.value = newValue;
        notifyMonitors();
    }

    protected void logValue() {
        if (LOG.isTraceEnabled()) {
            LOG.trace(getName() + "=" + value);
        }
    }
}