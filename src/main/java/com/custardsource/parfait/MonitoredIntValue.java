package com.aconex.monitoring;

import java.util.concurrent.atomic.AtomicInteger;

public class MonitoredIntValue extends MonitoredValue<AtomicInteger> {
	public MonitoredIntValue(String name, String description,
			MonitorableRegistry registry, Integer initialValue) {
		super(name, description, registry, new AtomicInteger(initialValue));
	}

	public MonitoredIntValue(String name, String description,
			Integer initialValue) {
		super(name, description, new AtomicInteger(initialValue));
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
