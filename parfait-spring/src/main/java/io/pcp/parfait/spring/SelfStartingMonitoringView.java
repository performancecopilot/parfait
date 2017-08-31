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

package io.pcp.parfait.spring;

import io.pcp.parfait.DynamicMonitoringView;
import io.pcp.parfait.MonitorableRegistry;
import io.pcp.parfait.MonitoringView;
import org.springframework.context.Lifecycle;

/**
 * Adapter between a normal MonitoringView and Spring's Lifecycle interface (which conveniently has the exact-same
 * methods).
 */
public class SelfStartingMonitoringView implements Lifecycle {

    private final DynamicMonitoringView dynamicMonitoringView;

    public SelfStartingMonitoringView(DynamicMonitoringView dynamicMonitoringView) {
        this.dynamicMonitoringView = dynamicMonitoringView;
    }

    /**
     * @deprecated,
     * Use <code>SelfStartingMonitoringView(DynamicMonitoringView dynamicMonitoringView)</code> instead.
     */
    @Deprecated
    public SelfStartingMonitoringView(MonitoringView monitoringView) {
        dynamicMonitoringView = new DynamicMonitoringView(monitoringView);
    }

    /**
     * @deprecated,
     * Use <code>SelfStartingMonitoringView(DynamicMonitoringView dynamicMonitoringView)</code> instead.
     */
    @Deprecated
    public SelfStartingMonitoringView(MonitoringView monitoringView, final long quietPeriodInMillis) {
        dynamicMonitoringView = new DynamicMonitoringView(monitoringView, quietPeriodInMillis);
    }

    /**
     * @deprecated,
     * Use <code>SelfStartingMonitoringView(DynamicMonitoringView dynamicMonitoringView)</code> instead.
     */
    @Deprecated
    public SelfStartingMonitoringView(MonitorableRegistry registry, MonitoringView monitoringView) {
        dynamicMonitoringView = new DynamicMonitoringView(registry, monitoringView);
    }

    /**
     * @deprecated,
     * Use <code>SelfStartingMonitoringView(DynamicMonitoringView dynamicMonitoringView)</code> instead.
     */
    @Deprecated
    public SelfStartingMonitoringView(MonitorableRegistry registry, MonitoringView monitoringView, final long quietPeriodInMillis) {
        dynamicMonitoringView = new DynamicMonitoringView(registry, monitoringView, quietPeriodInMillis);
    }

    @Override
    public void start() {
        dynamicMonitoringView.start();
    }

    @Override
    public void stop() {
        dynamicMonitoringView.stop();
    }

    @Override
    public boolean isRunning() {
        return dynamicMonitoringView.isRunning();
    }

    public static final long defaultQuietPeriod() {
        return DynamicMonitoringView.defaultQuietPeriod();
    }
}
