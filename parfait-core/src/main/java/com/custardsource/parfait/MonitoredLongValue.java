package com.custardsource.parfait;

import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link Monitorable} implementation for a free-running Long value.
 */
public class MonitoredLongValue extends MonitoredNumeric<AtomicLong> implements Counter {
	public MonitoredLongValue(String name, String description,
			MonitorableRegistry registry, Long initialValue) {
		super(name, description, registry, new AtomicLong(initialValue));
	}

	public MonitoredLongValue(String name, String description,
			Long initialValue) {
		super(name, description, MonitorableRegistry.DEFAULT_REGISTRY, new AtomicLong(initialValue));
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
