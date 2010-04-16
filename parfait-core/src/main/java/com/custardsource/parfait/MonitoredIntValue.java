package com.custardsource.parfait;

import javax.measure.unit.Unit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link Monitorable} implementation for a free-running Integer value.
 */
public class MonitoredIntValue extends MonitoredNumeric<AtomicInteger> {
	public MonitoredIntValue(String name, String description,
			MonitorableRegistry registry, Integer initialValue) {
		this(name, description, registry, initialValue, Unit.ONE);
	}

	public MonitoredIntValue(String name, String description,
			Integer initialValue) {
        this(name, description, MonitorableRegistry.DEFAULT_REGISTRY,
                initialValue, Unit.ONE);
	}

    public MonitoredIntValue(String name, String description, MonitorableRegistry registry,
            Integer initialValue, Unit<?> unit) {
        super(name, description, registry, new AtomicInteger(
                initialValue), unit);

    }

    /**
     * Convenience method to increment atomic numeric types.
     */
    public void inc() {
        value.incrementAndGet();
        notifyMonitors();
    }
    
    /**
     * Convenience method to decrement atomic numeric types.
     */
    public void dec() {
    	value.decrementAndGet();
        notifyMonitors();
    }
}
