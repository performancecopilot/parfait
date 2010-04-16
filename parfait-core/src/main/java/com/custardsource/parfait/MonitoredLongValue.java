package com.custardsource.parfait;

import javax.measure.unit.Unit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link Monitorable} implementation for a free-running Long value.
 */
public class MonitoredLongValue extends MonitoredNumeric<AtomicLong> implements Counter {
	public MonitoredLongValue(String name, String description,
			MonitorableRegistry registry, Long initialValue) {
		this(name, description, registry, initialValue, Unit.ONE);
	}

	public MonitoredLongValue(String name, String description,
			Long initialValue) {
		this(name, description, MonitorableRegistry.DEFAULT_REGISTRY, initialValue, Unit.ONE);
	}

    public MonitoredLongValue(String name, String description,
            MonitorableRegistry registry, Long initialValue, Unit<?> unit) {
        super(name, description, registry, new AtomicLong(initialValue), unit);
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

    @Override
    public void inc(long increment) {
        value.addAndGet(increment);
        notifyMonitors();
    }
}
