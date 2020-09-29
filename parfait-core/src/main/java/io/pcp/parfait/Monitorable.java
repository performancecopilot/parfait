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

/**
 * <p>
 * An interface to be implemented by any value that needs to be monitored using
 * the Parfait monitoring system.
 * </p>
 * <p>
 * An Monitorable primarily exists to return a value on demand (see
 * {@link #get()}) but must also be capable of providing metadata about the
 * semantics of a value so that various output sinks may treat the value
 * appropriately.
 * </p>
 * <p>
 * Monitors use JSR-363 Unit semantics to define their value scale, however if
 * the output sinks in use do not care about this value, or use it in any
 * meaningful way this may not need to be provided.
 * </p>
 * <p>
 * It is up to the particular {@link Monitor} implementation which uses a given
 * Monitorable to determine whether all Monitorables need to be 'pre-registered'
 * with the Monitor, or if dynamic addition of Monitorables over the lifetime of
 * an application is acceptable.
 * </p>
 * <p>
 * All monitor implementations must support concurrent access from multiple
 * threads.
 * </p>
 * 
 * @see MonitoredValue
 * @see MonitoredConstant
 * @see MonitoredCounter
 */
public interface Monitorable<T> {
    /**
     * @return the name of this Monitorable. Name must be unique across a single
     *         JVM. Typically uses a logging-framework-style dotted string
     *         ("java.vm.gc.count") but may be any arbitrary String value.
     */
    String getName();

    /**
     * @return a human-readable descriptive text for the Monitorable. May be
     *         used by tools to assist understanding of the metric.
     */
    String getDescription();

    /**
     * @return the JSR-363 Unit represented by the value of this Monitorable.
     *         This may be used to do comparisons and rate-conversions between
     *         metrics which do not share the same scale. Values which do not
     *         take a unit should use {@link tech.units.indriya.AbstractUnit#ONE};
     *         values for which no unit is sensible (e.g. String values) may
     *         return null.
     */
    Unit<?> getUnit();

    /**
     * @return the semantics of this Monitorable. Some tools may treat constant,
     *         variable, or monotonically-increasing values differently; this
     *         enables them to do so if required.
     */
    ValueSemantics getSemantics();

    /**
     * @return the type of the value returned by the {@link #get()} method.
     */
    Class<T> getType();

    /**
     * This method should never block and must return as quickly as possible.
     *
     * @return the current value of this Monitorable.
     */
    T get();

    /**
     * Attaches the provided Monitor. Once attached the Monitor will be notified
     * whenever the value of this Monitorable changes.
     *
     * @param m the Monitor to attach.
     */
    void attachMonitor(Monitor m);

    /**
     * Removed the provided Monitor from the list of attached Monitors.
     *
     * @param m the Monitor to remove.
     */
    void removeMonitor(Monitor m);
}
