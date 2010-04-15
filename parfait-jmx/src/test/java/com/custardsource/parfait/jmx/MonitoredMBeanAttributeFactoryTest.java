package com.custardsource.parfait.jmx;

import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.ValueSemantics;
import junit.framework.TestCase;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.measure.unit.Unit;
import java.lang.management.ManagementFactory;

public class MonitoredMBeanAttributeFactoryTest extends TestCase {

    public void testCanMonitorCompositeDataItem() throws InstanceNotFoundException,
            IntrospectionException, ReflectionException, AttributeNotFoundException, MBeanException {
        MonitoredMBeanAttributeFactory<Long> f = new MonitoredMBeanAttributeFactory<Long>(new MonitorableRegistry(), "aconex.free.memory",
                "", 1000, ValueSemantics.FREE_RUNNING, "java.lang:type=Memory", "HeapMemoryUsage", "max");
        assertEquals(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax(), f
                .getObject().get().longValue());
    }

    public void testCanMonitorStandardAttribute() throws InstanceNotFoundException,
            IntrospectionException, ReflectionException, AttributeNotFoundException, MBeanException {
        MonitoredMBeanAttributeFactory<Long> f = new MonitoredMBeanAttributeFactory<Long>(new MonitorableRegistry(),
                "aconex.system.startTime", "", 1000, ValueSemantics.FREE_RUNNING, "java.lang:type=Runtime", "StartTime", "");
        assertEquals(ManagementFactory.getRuntimeMXBean().getStartTime(), f.getObject().get().longValue());
    }
}
