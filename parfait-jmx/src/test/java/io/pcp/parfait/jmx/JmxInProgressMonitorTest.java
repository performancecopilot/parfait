package io.pcp.parfait.jmx;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;

import io.pcp.parfait.MonitorableRegistry;
import io.pcp.parfait.timing.EventTimer;
import io.pcp.parfait.timing.InProgressSnapshot;
import io.pcp.parfait.timing.LoggerSink;
import io.pcp.parfait.timing.StepMeasurementSink;
import io.pcp.parfait.timing.ThreadContext;
import io.pcp.parfait.timing.ThreadMetricSuite;
import io.pcp.parfait.timing.Timeable;
import org.junit.Test;

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
                new MonitorableRegistry(), ThreadMetricSuite.blank(), false, false,
                Collections.<StepMeasurementSink>singletonList(new LoggerSink()));
        timer.registerTimeable(t, "foo");
        timer.getCollector().startTiming(t, "stuff");
        InProgressSnapshot snapshot = InProgressSnapshot.capture(timer, context);
        TabularData data = JmxInProgressMonitor.TO_TABULAR_DATA.apply(snapshot);
        assertEquals(SimpleType.STRING, data.getTabularType().getRowType().getType("stringvalue"));
        assertEquals(SimpleType.STRING, data.getTabularType().getRowType().getType("intvalue"));
    }

}
