package com.custardsource.parfait.jmx;

import static org.junit.Assert.assertEquals;

import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;

import org.junit.Test;

import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.timing.EventTimer;
import com.custardsource.parfait.timing.InProgressSnapshot;
import com.custardsource.parfait.timing.ThreadContext;
import com.custardsource.parfait.timing.ThreadMetricSuite;
import com.custardsource.parfait.timing.Timeable;

public class JmxInProgressMonitorTest {
    @Test
    public void testValuesConvertedToStringsForExport() {
        ThreadContext context = new ThreadContext();
        context.put("stringvalue", "floop");
        context.put("intvalue", 99);

        Timeable t = new Timeable() {
            @Override
            public void setEventTimer(EventTimer timer) {
            }
        };
        
        EventTimer timer = new EventTimer("foo",
                new MonitorableRegistry(), ThreadMetricSuite.blank(), false, false);
        timer.registerTimeable(t, "foo");
        timer.getCollector().startTiming(t, "stuff");
        InProgressSnapshot snapshot = InProgressSnapshot.capture(timer, context);
        TabularData data = JmxInProgressMonitor.TO_TABULAR_DATA.apply(snapshot);
        assertEquals(SimpleType.STRING, data.getTabularType().getRowType().getType("stringvalue"));
        assertEquals(SimpleType.STRING, data.getTabularType().getRowType().getType("intvalue"));
    }

}
