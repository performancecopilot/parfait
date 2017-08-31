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

import java.util.Collection;

/**
 * An output bridge for a particular set of Monitorables. A MonitoringView
 * provides a convenient lifecycle for an output destination to know when all
 * metrics have been set up and initialized, and are ready to be output to the
 * destination. When {@link #startMonitoring(java.util.Collection)} is called, all Monitorables should be
 * initialized and ready to be written to the output. {@link #stopMonitoring(java.util.Collection)} should be
 * (but, of course, is not guaranteed to be) called when the monitoring
 * subsystem is being shut down.
 */
public interface MonitoringView {

    /**
     * Instructs the view to begin its work using the set of Monitorable instances
     * as the basis for whatever it is this 'view' wants to do with it (say, start externalising
     * the state of the metrics to an external file).
     *
     * It is expected that this view tracks its own start/stop state, and that clients
     * of this implementation should correctly call {@link #stopMonitoring(java.util.Collection)}  before recalling startMonitoring.
     *
     * @param monitorables the Collection of metrics to start monitoring
     */
    void startMonitoring(Collection<Monitorable<?>> monitorables);

    /**
     * Stops monitoring updates on the Monitorables in the provided registry, any
     * updates to any of the monitorables passed in will no longer be tracked by this View
     *
     * @param monitorables the Collection of metrics to stop monitoring
     */
    void stopMonitoring(Collection<Monitorable<?>> monitorables);

    /**
     * @return whether or not this view has been started with {@link #startMonitoring(java.util.Collection)} ()}
     */
    boolean isRunning();
}
