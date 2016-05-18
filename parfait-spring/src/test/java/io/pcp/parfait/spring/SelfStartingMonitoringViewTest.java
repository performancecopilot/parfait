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
