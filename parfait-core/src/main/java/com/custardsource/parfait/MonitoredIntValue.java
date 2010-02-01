package com.custardsource.parfait;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link Monitorable} implementation for a free-running Integer value.
 */
public class MonitoredIntValue extends MonitoredNumeric<AtomicInteger> {
	public MonitoredIntValue(String name, String description,
			MonitorableRegistry registry, Integer initialValue) {
		super(name, description, registry, new AtomicInteger(initialValue));
	}

	public MonitoredIntValue(String name, String description,
			Integer initialValue) {
        super(name, description, MonitorableRegistry.DEFAULT_REGISTRY, new AtomicInteger(
                initialValue));
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
