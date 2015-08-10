package com.custardsource.parfait;

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


public class MonitoringViewDelegateTest {

    @Mock
    MonitorableRegistry monitorableRegistry;

    List<Monitorable<?>> monitorables = Collections.<Monitorable<?>>singletonList(new DummyMonitorable("foo"));

    @Mock
    MonitoringView monitoringView;

    private MonitoringViewDelegate monitoringViewDelegate;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        monitoringViewDelegate = new MonitoringViewDelegate(monitorableRegistry, monitoringView, 2000);
        when(monitorableRegistry.getMonitorables()).thenReturn(monitorables);
    }

    @Test
    public void startAndStopShouldStartAndStopMonitoringOnWrappedView() throws Exception {

        monitoringViewDelegate.start();

        verify(monitoringView).startMonitoring(monitorables);

        monitoringViewDelegate.stop();

        verify(monitoringView).stopMonitoring(monitorables);

    }

    @Test
    public void isRunningShouldDelegateToWrappedView() throws Exception {

        when(monitoringView.isRunning()).thenReturn(false);
        assertFalse(monitoringViewDelegate.isRunning());
        monitoringViewDelegate.start();
        when(monitoringView.isRunning()).thenReturn(true);
        assertTrue(monitoringViewDelegate.isRunning());
    }
}
