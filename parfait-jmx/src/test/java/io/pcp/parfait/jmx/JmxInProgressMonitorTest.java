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
