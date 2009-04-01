package com.aconex.monitoring;

import java.util.concurrent.atomic.AtomicLong;

public class MonitoredLongValue extends MonitoredValue<AtomicLong> {
	public MonitoredLongValue(String name, String description,
			MonitorableRegistry registry, Long initialValue) {
		super(name, description, registry, new AtomicLong(initialValue));
	}

	public MonitoredLongValue(String name, String description,
			Long initialValue) {
		super(name, description, new AtomicLong(initialValue));
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
