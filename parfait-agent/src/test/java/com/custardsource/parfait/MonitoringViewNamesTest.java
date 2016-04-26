package com.custardsource.parfait;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

public class MonitoringViewNamesTest {
    @Test
    public void checkJvmProcessString() {
        // expecting to be of form "pid@host" (need new JVM-specific code if not)
        String name = MonitoringViewProperties.getRuntimeName();
        assertNotNull(name);
        assertNotSame(name, "");

        String[] pidAndHost = name.split("@", 2);
        assertNotNull(pidAndHost);
        assertEquals(pidAndHost.length, 2);

        int pid = Integer.parseInt(pidAndHost[0]);
        assertEquals(pidAndHost.length, 2);
    }

    @Test
    public void checkProcessIdentifierExtraction() {
        assertEquals(MonitoringViewProperties.getFallbackName(""), MonitoringViewProperties.PARFAIT);
        assertEquals(MonitoringViewProperties.getFallbackName("12345"), MonitoringViewProperties.PARFAIT);
        assertEquals(MonitoringViewProperties.getFallbackName("@localhost"), MonitoringViewProperties.PARFAIT);
        assertEquals(MonitoringViewProperties.getFallbackName("12345@localhost"), MonitoringViewProperties.PARFAIT + "12345");
    }

    @Test
    public void checkDefaultNameResults() {
        assertEquals(MonitoringViewProperties.getDefaultName(null, null, null), MonitoringViewProperties.PARFAIT);
        assertEquals(MonitoringViewProperties.getDefaultName("foo", null, null), "foo");
        assertEquals(MonitoringViewProperties.getDefaultName("foo", "bar", null), "foo");
        assertEquals(MonitoringViewProperties.getDefaultName(null, "bar", null), "bar");
        assertEquals(MonitoringViewProperties.getDefaultName(null, null, "baz"), MonitoringViewProperties.PARFAIT);
    }
}
