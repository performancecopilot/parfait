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

import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompositeMonitoringViewTest {

    @Mock
    MonitoringView monitoringView1;

    @Mock
    MonitoringView monitoringView2;

    CompositeMonitoringView compositeMonitoringView;

    @Mock
    Collection<Monitorable<?>> monitorables;

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);

        when(monitoringView1.isRunning()).thenReturn(false);
        when(monitoringView2.isRunning()).thenReturn(true);

        when(monitorables.size()).thenReturn(2);

        this.compositeMonitoringView = new CompositeMonitoringView(monitoringView1, monitoringView2);
    }

    @Test
    public void startMonitoringShouldStartAllViews() {

        compositeMonitoringView.startMonitoring(monitorables);

        verify(monitoringView1).startMonitoring(monitorables);
        verify(monitoringView2).startMonitoring(monitorables);

    }

    @Test
    public void stopMonitoringShouldStopAllViews() {
        compositeMonitoringView.stopMonitoring(monitorables);

        verify(monitoringView1).stopMonitoring(monitorables);
        verify(monitoringView2).stopMonitoring(monitorables);
    }

    @Test
    public void isRunningReturnsTrueIfAnyRunning() {
        assertTrue(compositeMonitoringView.isRunning());
    }

    @Test
    public void isRunningReturnsFalseIfAllAreNotRunning() {
        CompositeMonitoringView monitoringView = new CompositeMonitoringView(monitoringView1);
        assertFalse(monitoringView.isRunning());
    }
}
