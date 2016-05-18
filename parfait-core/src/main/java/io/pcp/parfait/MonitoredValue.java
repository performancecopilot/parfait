package io.pcp.parfait;

import static tec.units.ri.AbstractUnit.ONE;

import javax.measure.Unit;

/**
 * MonitoredValue provides a convenient implementation of {@link Monitorable}
 * for free-running values that are updatable through a single set method call.
 * <p>
 * A free-running value is a value that increments and decrements at-will over
 * time. This is essentially a "point in time" measurement. An example of an
 * instantaneous value is the number of active HTTP sessions.
 * <p>
 * It is recommended that monotonically-increasing counters be implemented using
 * the class {@link MonitoredCounter} in preference to this class.
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
        this(name, description, registry, initialValue, ONE);
    }

    public MonitoredValue(String name, String description, MonitorableRegistry registry,
            T initialValue, Unit<?> unit) {
        super(name, description, registry, initialValue, unit, ValueSemantics.FREE_RUNNING);
    }
}
