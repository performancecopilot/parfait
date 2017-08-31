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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class DynamicMonitoringViewTest {

    @Mock
    MonitorableRegistry monitorableRegistry;

    List<Monitorable<?>> monitorables = Collections.<Monitorable<?>>singletonList(new DummyMonitorable("foo"));

    @Mock
    MonitoringView monitoringView;

    private DynamicMonitoringView dynamicMonitoringView;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        dynamicMonitoringView = new DynamicMonitoringView(monitorableRegistry, monitoringView, 2000);
        when(monitorableRegistry.getMonitorables()).thenReturn(monitorables);
    }

    @Test
    public void startAndStopShouldStartAndStopMonitoringOnWrappedView() throws Exception {

        dynamicMonitoringView.start();

        verify(monitoringView).startMonitoring(monitorables);

        dynamicMonitoringView.stop();

        verify(monitoringView).stopMonitoring(monitorables);

    }

    @Test
    public void isRunningShouldDelegateToWrappedView() throws Exception {

        when(monitoringView.isRunning()).thenReturn(false);
        assertFalse(dynamicMonitoringView.isRunning());
        dynamicMonitoringView.start();
        when(monitoringView.isRunning()).thenReturn(true);
        assertTrue(dynamicMonitoringView.isRunning());
    }
}
