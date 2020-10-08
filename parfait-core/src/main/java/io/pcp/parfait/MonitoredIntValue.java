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
