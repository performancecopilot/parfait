package com.custardsource.parfait.spring;

import com.custardsource.parfait.DummyMonitorable;
import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.MonitoringView;
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


public class SelfStartingMonitoringViewTest {

    @Mock
    MonitorableRegistry monitorableRegistry;

    List<Monitorable<?>> monitorables = Collections.<Monitorable<?>>singletonList(new DummyMonitorable("foo"));

    @Mock MonitoringView monitoringView;
    
    private SelfStartingMonitoringView selfStartingMonitoringView;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        selfStartingMonitoringView = new SelfStartingMonitoringView(monitorableRegistry, monitoringView, 2000);
        when(monitorableRegistry.getMonitorables()).thenReturn(monitorables);
    }

    @Test
    public void testStartAndStop() throws Exception {

        selfStartingMonitoringView.start();

        verify(monitoringView).startMonitoring(monitorables);

        selfStartingMonitoringView.stop();

        verify(monitoringView).stopMonitoring(monitorables);

    }


    @Test
    public void testIsRunning() throws Exception {

        when(monitoringView.isRunning()).thenReturn(false);
        assertFalse(selfStartingMonitoringView.isRunning());
        selfStartingMonitoringView.start();
        when(monitoringView.isRunning()).thenReturn(true);
        assertTrue(selfStartingMonitoringView.isRunning());
    }
}
