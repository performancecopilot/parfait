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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;

public class CompositeMonitoringView implements MonitoringView{

    private final List<MonitoringView> monitoringViews;

    public CompositeMonitoringView(MonitoringView... monitoringViews) {
        this.monitoringViews = copyOf(monitoringViews);
    }

    @Override
    public void startMonitoring(Collection<Monitorable<?>> monitorables) {
        for (MonitoringView monitoringView : monitoringViews) {
            monitoringView.startMonitoring(monitorables);
        }
    }

    @Override
    public void stopMonitoring(Collection<Monitorable<?>> monitorables) {
        for (MonitoringView monitoringView : monitoringViews) {
            monitoringView.stopMonitoring(monitorables);
        }
    }

    @Override
    /**
     * For a Composite view, isRunning() is true if any of the underlying views are still running
     */
    public boolean isRunning() {
        return Iterators.any(monitoringViews.iterator(), new Predicate<MonitoringView>() {
            @Override
            public boolean apply(MonitoringView mv) {
                return mv.isRunning();
            }
        });
    }
}
