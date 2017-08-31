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

package io.pcp.parfait.pcp;

import io.pcp.parfait.DummyMonitorable;
import io.pcp.parfait.DynamicMonitoringView;
import io.pcp.parfait.MonitorableRegistry;
import io.pcp.parfait.dxm.IdentifierSourceSet;
import io.pcp.parfait.dxm.InMemoryByteBufferFactory;
import io.pcp.parfait.dxm.PcpMmvWriter;
import io.pcp.parfait.dxm.PcpWriter;
import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;
import org.junit.Test;

import java.nio.ByteBuffer;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PcpMonitorBridgeIntegrationTest {

    @Test
    public void metricsAddedAfterStartShouldAppearEventually() {

        final InMemoryByteBufferFactory bufferFactory = new InMemoryByteBufferFactory();
        final PcpWriter writer = new PcpMmvWriter(bufferFactory, IdentifierSourceSet.DEFAULT_SET);
        final PcpMonitorBridge pcpMonitorBridge = new PcpMonitorBridge(writer);
        final MonitorableRegistry registry = new MonitorableRegistry();
        final DynamicMonitoringView dynamicallyModifiedView = new DynamicMonitoringView(registry, pcpMonitorBridge, 1000);

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
