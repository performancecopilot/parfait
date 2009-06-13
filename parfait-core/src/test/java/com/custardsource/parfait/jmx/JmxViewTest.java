package com.custardsource.parfait.jmx;

import java.io.IOException;

import javax.management.openmbean.CompositeData;

import junit.framework.TestCase;

import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.MonitoredValue;

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

        jmx = new JmxView(registry);
    }

    public void tearDown() {
        jmx.stop();
    }

    public void testSupportsAllTypes() throws IOException, InterruptedException {
        jmx.start();

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
