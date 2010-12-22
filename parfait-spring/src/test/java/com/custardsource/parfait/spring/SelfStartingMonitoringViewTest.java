package com.custardsource.parfait.spring;

import com.custardsource.parfait.DummyMonitorable;
import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.MonitoringView;
import com.custardsource.parfait.dxm.IdentifierSourceSet;
import com.custardsource.parfait.dxm.InMemoryByteBufferFactory;
import com.custardsource.parfait.dxm.PcpMmvWriter;
import com.custardsource.parfait.dxm.PcpWriter;
import com.custardsource.parfait.pcp.PcpMonitorBridge;
import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class SelfStartingMonitoringViewTest {

    @Mock
    MonitorableRegistry monitorableRegistry;

    List<Monitorable<?>> monitorables = Collections.<Monitorable<?>>singletonList(new DummyMonitorable("foo"));

    @Mock
    MonitoringView monitoringView;

    private SelfStartingMonitoringView selfStartingMonitoringView;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        selfStartingMonitoringView = new SelfStartingMonitoringView(monitorableRegistry, monitoringView, 2000);
        when(monitorableRegistry.getMonitorables()).thenReturn(monitorables);
    }

    @Test
    public void startAndStopShouldStartAndStopMonitoringOnWrappedView() throws Exception {

        selfStartingMonitoringView.start();

        verify(monitoringView).startMonitoring(monitorables);

        selfStartingMonitoringView.stop();

        verify(monitoringView).stopMonitoring(monitorables);

    }


    @Test
    public void isRunningShouldDelegateToWrappedView() throws Exception {

        when(monitoringView.isRunning()).thenReturn(false);
        assertFalse(selfStartingMonitoringView.isRunning());
        selfStartingMonitoringView.start();
        when(monitoringView.isRunning()).thenReturn(true);
        assertTrue(selfStartingMonitoringView.isRunning());
    }

    @Test
    public void metricsAddedAfterStartShouldAppearEventually() {

        final InMemoryByteBufferFactory bufferFactory = new InMemoryByteBufferFactory();
        final PcpWriter writer = new PcpMmvWriter(bufferFactory, IdentifierSourceSet.DEFAULT_SET);
        final PcpMonitorBridge pcpMonitorBridge = new PcpMonitorBridge(writer);
        final MonitorableRegistry registry = new MonitorableRegistry();
        final SelfStartingMonitoringView dynamicallyModifiedView = new SelfStartingMonitoringView(registry, pcpMonitorBridge, 1000);

        registry.register(new DummyMonitorable("foo"));

        dynamicallyModifiedView.start();

        ByteBuffer buffer = bufferFactory.getAllocatedBuffer();

        assertEquals(1, bufferFactory.getNumAllocations());

        assertBufferContainsExpectedStrings(buffer, "foo");

        registry.register(new DummyMonitorable("eek"));

        // TODO Mad mad MAD props for changing this to use Scheduler; QuiescentRegistryListenerTest was failing for me ~50% of the time for some reason, and you really do notice that 2.5s. Would rather not do this if possible....

        try {
            Thread.sleep(2500);
        } catch (Exception e) {
        }

        assertEquals("Expected only 2 Buffer allocations", 2, bufferFactory.getNumAllocations());

        buffer = bufferFactory.getAllocatedBuffer();

        assertBufferContainsExpectedStrings(buffer, "eek");

    }

    /**
     * we cast the string to US-ASCII charset because that's what PCP uses and then find it in the ByteBuffer array
     * to ensure it's there.
     */
    private void assertBufferContainsExpectedStrings(ByteBuffer buffer, final String expectedString) {
        assertTrue(Bytes.indexOf(buffer.array(), expectedString.getBytes(Charsets.US_ASCII)) >= 0);
    }
}
