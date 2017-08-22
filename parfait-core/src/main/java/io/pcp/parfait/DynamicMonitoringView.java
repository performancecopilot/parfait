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

import com.google.common.collect.Lists;

import java.util.Collection;

public class DynamicMonitoringView {

    private static final long DEFAULT_QUIET_PERIOD = 5000L;
    private final long quietPeriodInMillis;
    private final MonitoringView monitoringView;
    private final MonitorableRegistry monitorableRegistry;
    private Collection<Monitorable<?>> previouslySeenMonitorables = Lists.newArrayList();
    private QuiescentRegistryListener quiescentRegistryListener;

    public DynamicMonitoringView(MonitoringView monitoringView) {
        this(monitoringView, DEFAULT_QUIET_PERIOD);
    }

    public DynamicMonitoringView(MonitoringView monitoringView, final long quietPeriodInMillis) {
        this(MonitorableRegistry.DEFAULT_REGISTRY, monitoringView, quietPeriodInMillis);
    }

    public DynamicMonitoringView(MonitorableRegistry registry, MonitoringView monitoringView) {
        this(registry, monitoringView, DEFAULT_QUIET_PERIOD);
    }

    public DynamicMonitoringView(MonitorableRegistry registry, MonitoringView monitoringView, final long quietPeriodInMillis) {
        this.monitoringView = monitoringView;
        this.monitorableRegistry = registry;
        this.quietPeriodInMillis = quietPeriodInMillis;
    }

    public void start() {
        previouslySeenMonitorables = monitorableRegistry.getMonitorables();
        monitoringView.startMonitoring(previouslySeenMonitorables);
        this.quiescentRegistryListener = new QuiescentRegistryListener(new Runnable() {
            @Override
            public void run() {
                stop();
                start();
            }
        }, quietPeriodInMillis);
        monitorableRegistry.addRegistryListener(quiescentRegistryListener);
    }

    public void stop() {
        monitorableRegistry.removeRegistryListener(quiescentRegistryListener);
        this.quiescentRegistryListener.stop();
        monitoringView.stopMonitoring(previouslySeenMonitorables);
    }

    public boolean isRunning() {
        return monitoringView.isRunning();
    }

    public static final long defaultQuietPeriod() {
        return DEFAULT_QUIET_PERIOD;
    }
}
