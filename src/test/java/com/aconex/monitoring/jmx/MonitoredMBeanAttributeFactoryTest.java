package com.aconex.monitoring.jmx;

import java.lang.management.ManagementFactory;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import junit.framework.TestCase;

public class MonitoredMBeanAttributeFactoryTest extends TestCase {

    public void testCanMonitorCompositeDataItem() throws InstanceNotFoundException,
            IntrospectionException, ReflectionException, AttributeNotFoundException, MBeanException {
        MonitoredMBeanAttributeFactory<Long> f = new MonitoredMBeanAttributeFactory<Long>("aconex.free.memory",
                "", 1000, "java.lang:type=Memory", "HeapMemoryUsage", "max");
        assertEquals(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax(), f
                .getObject().get().longValue());
    }

    public void testCanMonitorStandardAttribute() throws InstanceNotFoundException,
            IntrospectionException, ReflectionException, AttributeNotFoundException, MBeanException {
        MonitoredMBeanAttributeFactory<Long> f = new MonitoredMBeanAttributeFactory<Long>(
                "aconex.system.startTime", "", 1000, "java.lang:type=Runtime", "StartTime");
        assertEquals(ManagementFactory.getRuntimeMXBean().getStartTime(), f.getObject().get().longValue());
    }
}
