package com.custardsource.parfait;

import java.util.concurrent.atomic.AtomicLong;

import javax.measure.unit.Unit;

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
        inc(1);
    }

    @Override
    public void inc(int delta) {
        value.addAndGet(delta);
        notifyMonitors();
    }

    /**
     * Convenience method to decrement atomic numeric types.
     */
    public void dec() {
        dec(1);
    }

    @Override
    public void dec(int delta) {
        inc(-delta);
    }

    @Override
    public void inc(long increment) {
        value.addAndGet(increment);
        notifyMonitors();
    }
}
