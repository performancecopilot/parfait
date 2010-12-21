package com.custardsource.parfait;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;

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
    public void testStartMonitoring() {

        compositeMonitoringView.startMonitoring(monitorables);

        verify(monitoringView1).startMonitoring(monitorables);
        verify(monitoringView2).startMonitoring(monitorables);

    }

    @Test
    public void testStopMonitoring() {
        compositeMonitoringView.stopMonitoring(monitorables);

        verify(monitoringView1).stopMonitoring(monitorables);
        verify(monitoringView2).stopMonitoring(monitorables);
    }

    @Test
    public void testIsRunning() {
        assertTrue(compositeMonitoringView.isRunning());
    }
}
