/*
 * Copyright 2009-2017 Aconex
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.pcp.parfait;

import static tech.units.indriya.AbstractUnit.ONE;

import java.util.concurrent.atomic.AtomicLong;
import javax.measure.Unit;

/**
 * A MonitoredCounter is a useful implementation of {@link Monitorable} specifically for
 * implementing long-valued counters.
 * <p>
 * This class should be used to measure incrementing counter values only. For any other values, use
 * {@link MonitoredValue} or another subclass of {@link SettableValue}.
 * <p>
 * In Parfait terms, a counter is a value that increments over time due to an event. An example of a counter might
 * the number of JMS messages sent or Garbage collections completed. Note that this class explicitly provides an atomic increment
 * operation only. Values must not decrement or be set to an arbitrary value.
 */
public class MonitoredCounter extends AbstractMonitorable<Long> implements Counter {
    private final AtomicLong value = new AtomicLong(0L);

    /**
     * Creates a new MonitoredCounter against
     * {@link MonitorableRegistry#DEFAULT_REGISTRY the default registry} with no
     * unit semantics.
     */
    public MonitoredCounter(String name, String description) {
    	this(name, description, MonitorableRegistry.DEFAULT_REGISTRY);
    }

    /**
     * Creates a new MonitoredCounter against the given registry with no unit
     * semantics.
     */
    public MonitoredCounter(String name, String description, MonitorableRegistry registry) {
        this(name, description, registry, ONE);
    }

    /**
     * Creates a new MonitoredCounter against
     * {@link MonitorableRegistry#DEFAULT_REGISTRY the default registry}
     */
    public MonitoredCounter(String name, String description, Unit<?> unit) {
        this(name, description, MonitorableRegistry.DEFAULT_REGISTRY, unit);
    }

    /**
     * Creates a new MonitoredCounter against the provided
     * {@link MonitorableRegistry} with the given unit semantics.
     */
    public MonitoredCounter(String name, String description, MonitorableRegistry registry,
            Unit<?> unit) {
        super(name, description, Long.class, unit, ValueSemantics.MONOTONICALLY_INCREASING);
        registerSelf(registry);
    }

    @Override
    public Long get() {
        return value.get();
    }

    /**
     * <p>Reset the counter to a specific value. This is <em>not</em> the typical use of this class;
     * this class' value is typically monotonically increasing, and should not roam freely, making
     * {@link #inc(long)} the more common usage </p>
     * <p>Use this method in the case of a 'reset' or similar functionality, or in the case where
     * values are being extracted from a monotonically-increasing source and we need to use
     * that source's value verbatim.</p>
     */
    public void set(long newValue) {
        value.set(newValue);
        notifyMonitors();
    }

    /**
     * Increments the counter by a given value.
     * 
     * @param value
     *            the amount to increment. Should be non-negative
     */
    public void inc(long value) {
        this.value.addAndGet(value);
        notifyMonitors();
    }

    /**
     * Increments the counter by one.
     */
    public void inc() {
        value.incrementAndGet();
        notifyMonitors();
    }
}
