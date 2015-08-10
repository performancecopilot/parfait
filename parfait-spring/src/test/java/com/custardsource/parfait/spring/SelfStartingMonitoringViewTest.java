package com.custardsource.parfait.spring;

import com.custardsource.parfait.MonitoringViewDelegate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class SelfStartingMonitoringViewTest {

    private SelfStartingMonitoringView selfStartingMonitoringView;

    @Mock
    MonitoringViewDelegate monitoringViewDelegate;

    @Before
    public void setUp() {
        initMocks(this);
        selfStartingMonitoringView = new SelfStartingMonitoringView(monitoringViewDelegate);
    }

    @Test
    public void shouldInvokeDelegateStart() throws Exception {
        selfStartingMonitoringView.start();
        verify(monitoringViewDelegate).start();
    }

    @Test
    public void shouldInvokeDelegateStop() throws Exception {
        selfStartingMonitoringView.stop();
        verify(monitoringViewDelegate).stop();
    }

    @Test
    public void shouldInvokeDelegateIsRunning() throws Exception {
        selfStartingMonitoringView.isRunning();
        verify(monitoringViewDelegate).isRunning();
    }
}
