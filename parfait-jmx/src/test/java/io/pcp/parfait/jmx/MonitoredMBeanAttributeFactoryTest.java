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

package io.pcp.parfait.jmx;

import junit.framework.TestCase;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import java.io.IOException;
import java.lang.management.ManagementFactory;

public class MonitoredMBeanAttributeFactoryTest extends TestCase {

    public void testCanMonitorCompositeDataItem() throws InstanceNotFoundException,
            IntrospectionException, ReflectionException, AttributeNotFoundException, MBeanException, IOException {
        MonitoredMBeanAttributeFactory<Long> f = new MonitoredMBeanAttributeFactory<Long>("aconex.free.memory",
                "", "java.lang:type=Memory", "HeapMemoryUsage", "max");
        assertEquals(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax(), f
                .getObject().get().longValue());
    }

    public void testCanMonitorStandardAttribute() throws InstanceNotFoundException,
            IntrospectionException, ReflectionException, AttributeNotFoundException, MBeanException, IOException {
        MonitoredMBeanAttributeFactory<Long> f = new MonitoredMBeanAttributeFactory<Long>(
                "aconex.system.startTime", "", "java.lang:type=Runtime", "StartTime", "");
        assertEquals(ManagementFactory.getRuntimeMXBean().getStartTime(), f.getObject().get().longValue());
    }
}
