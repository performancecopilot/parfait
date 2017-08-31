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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class SelfStartingMonitoringViewTest {

    private SelfStartingMonitoringView selfStartingMonitoringView;

    @Mock
    DynamicMonitoringView dynamicMonitoringView;

    @Before
    public void setUp() {
        initMocks(this);
        selfStartingMonitoringView = new SelfStartingMonitoringView(dynamicMonitoringView);
    }

    @Test
    public void shouldInvokeDelegateStart() throws Exception {
        selfStartingMonitoringView.start();
        verify(dynamicMonitoringView).start();
    }

    @Test
    public void shouldInvokeDelegateStop() throws Exception {
        selfStartingMonitoringView.stop();
        verify(dynamicMonitoringView).stop();
    }

    @Test
    public void shouldInvokeDelegateIsRunning() throws Exception {
        selfStartingMonitoringView.isRunning();
        verify(dynamicMonitoringView).isRunning();
    }
}
