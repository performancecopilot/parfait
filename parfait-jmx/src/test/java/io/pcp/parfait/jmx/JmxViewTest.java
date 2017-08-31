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

import io.pcp.parfait.MonitorableRegistry;
import io.pcp.parfait.MonitoredValue;
import junit.framework.TestCase;

import javax.management.openmbean.CompositeData;
import java.io.IOException;

public class JmxViewTest extends TestCase {
    private MonitoredValue<Boolean> booleanValue = null;

    private MonitoredValue<Integer> intValue = null;

    private MonitoredValue<Long> longValue = null;
    private MonitoredValue<Double> doubleValue = null;
    private MonitoredValue<String> stringValue = null;
    
    private MonitorableRegistry registry = new MonitorableRegistry();

    private JmxView jmx = null;

    public JmxViewTest() {
    }

    public void setUp() {
        booleanValue = new MonitoredValue<Boolean>("boolean.value", "boolean.value.desc", registry, true);
        intValue = new MonitoredValue<Integer>("int.value", "int.value.desc", registry, 1);
        longValue = new MonitoredValue<Long>("long.value", "long.value.desc", registry, 1l);
        doubleValue = new MonitoredValue<Double>("double.value", "double.value.desc", registry, 1d);
        stringValue = new MonitoredValue<String>("string.value", "string.value.desc", registry, "!");

        jmx = new JmxView();
    }

    public void tearDown() {
        jmx.stopMonitoring(registry.getMonitorables());
    }

    public void testSupportsAllTypes() throws IOException, InterruptedException {
        jmx.startMonitoring(registry.getMonitorables());

        checkDataValues();

        booleanValue.set(false);
        checkDataValues();

        booleanValue.set(true);
        checkDataValues();

        intValue.set(0);
        checkDataValues();

        intValue.set(Integer.MAX_VALUE);
        checkDataValues();

        intValue.set(Integer.MIN_VALUE);
        checkDataValues();

        intValue.set(1234567890);
        checkDataValues();

        longValue.set(0l);
        checkDataValues();

        longValue.set(Long.MAX_VALUE);
        checkDataValues();

        longValue.set(Long.MIN_VALUE);
        checkDataValues();

        longValue.set(1234567891012345679l);
        checkDataValues();

        doubleValue.set(0d);
        checkDataValues();

        doubleValue.set(Double.MAX_VALUE);
        checkDataValues();

        doubleValue.set(Double.MIN_VALUE);
        checkDataValues();

        doubleValue.set(Double.NEGATIVE_INFINITY);
        checkDataValues();

        doubleValue.set(Double.POSITIVE_INFINITY);
        checkDataValues();

        doubleValue.set(Double.NaN);
        checkDataValues();

        doubleValue.set(1234567891.012345679d);
        checkDataValues();

        stringValue.set("");
        checkDataValues();

        stringValue.set(createString(500));
        checkDataValues();
    }

    private String createString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(Math.max(1, i & 255));
        }
        return sb.toString();
    }

    private void checkDataValues() {
        
        CompositeData data = jmx.getExposedMetrics();

        assertEquals(booleanValue.get(), data.get("boolean.value"));
        assertEquals(doubleValue.get(), data.get("double.value"));
        assertEquals((int) intValue.get(), data.get("int.value"));
        assertEquals((long) longValue.get(), data.get("long.value"));
        assertEquals(stringValue.get(), data.get("string.value"));
    }
}
