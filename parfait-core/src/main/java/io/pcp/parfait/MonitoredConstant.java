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

import static tec.uom.se.AbstractUnit.ONE;
import javax.measure.Unit;

/**
 * MonitoredConstant provides an implementation of {@link Monitorable} for
 * simple values that are rarely (read: never) updated, once initialised.
 * <p>
 * This class should be used as for values which rarely change, such as the
 * number of installed CPUs or the application version number.
 * <p>
 * A setter exists for those cases where the value is not known until after
 * creation (pre-registration of the Monitorable is required, but the value is
 * not known at creation time), but this should not be called as a matter of
 * course.
 */
public class MonitoredConstant<T> extends SettableValue<T> {
    public MonitoredConstant(String name, String description, T initialValue) {
        this(name, description, MonitorableRegistry.DEFAULT_REGISTRY,
                initialValue);
    }

    public MonitoredConstant(String name, String description, T initialValue, Unit<?> unit) {
        this(name, description, MonitorableRegistry.DEFAULT_REGISTRY, initialValue, unit);
    }

    public MonitoredConstant(String name, String description, MonitorableRegistry registry,
            T initialValue) {
        this(name, description, registry, initialValue, ONE);
    }

    public MonitoredConstant(String name, String description, MonitorableRegistry registry,
            T initialValue, Unit<?> unit) {
        super(name, description, registry, initialValue, unit, ValueSemantics.CONSTANT);
    }
}
