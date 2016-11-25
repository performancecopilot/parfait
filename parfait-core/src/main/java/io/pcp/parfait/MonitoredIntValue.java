package io.pcp.parfait;

import static tec.uom.se.AbstractUnit.ONE;

import java.util.concurrent.atomic.AtomicInteger;
import javax.measure.Unit;

/**
 * {@link Monitorable} implementation for a free-running Integer value.
 */
public class MonitoredIntValue extends MonitoredNumeric<AtomicInteger> {
	public MonitoredIntValue(String name, String description,
			MonitorableRegistry registry, Integer initialValue) {
		this(name, description, registry, initialValue, ONE);
	}

	public MonitoredIntValue(String name, String description,
			Integer initialValue) {
        this(name, description, MonitorableRegistry.DEFAULT_REGISTRY, initialValue, ONE);
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
}
