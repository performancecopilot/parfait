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

import javax.measure.Unit;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * A base class for Monitorables which can have their value set to an arbitrary
 * value at runtime.
 */
abstract class SettableValue<T> extends AbstractMonitorable<T> {
    protected volatile T value;

    @SuppressWarnings("unchecked")
    protected SettableValue(String name, String description, MonitorableRegistry registry,
            T initialValue, Unit<?> unit, ValueSemantics semantics) {
        super(name, description, (Class<T>) initialValue.getClass(), unit, semantics);
        Preconditions.checkNotNull(initialValue, "Monitored value can not be null");
        this.value = initialValue;
        registerSelf(registry);
    }

    @Override
    public T get() {
        return value;
    }

    /**
     * Sets the current value of this Monitorable. Some level of optimization is
     * performed to ensure that spurious updates are not sent out (i.e. when the
     * new value is the same as the old), but this is optimized for performance
     * and no guarantee is made that such notifications will not be sent.
     */
    public void set(T newValue) {
        Preconditions.checkNotNull(newValue, "Monitored value can not be null");
        if (Objects.equal(this.value, newValue)) {
            return;
        }
        this.value = newValue;
        notifyMonitors();
    }
}
