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

package io.pcp.parfait.dropwizard;

import javax.measure.Unit;

import io.pcp.parfait.AbstractMonitorable;
import io.pcp.parfait.ValueSemantics;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class NonSelfRegisteringSettableValue<T> extends AbstractMonitorable<T> {

    private T value;

    @SuppressWarnings({"unchecked", "PMD.ExcessiveParameterList"})
    public NonSelfRegisteringSettableValue(String name, String description, Unit<?> unit, T initialValue, ValueSemantics valueSemantics) {
        super(name, description, (Class<T>) initialValue.getClass(), unit, valueSemantics);
        value = initialValue;
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
