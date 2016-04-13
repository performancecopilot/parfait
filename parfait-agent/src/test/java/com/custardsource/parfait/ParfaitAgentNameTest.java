package com.custardsource.parfait;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

public class ParfaitAgentNameTest {
    @Test
    public void checkJvmProcessString() {
        // expecting to be of form "pid@host" (need new JVM-specific code if not)
        String name = ParfaitAgent.getRuntimeName();
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
        assertEquals(ParfaitAgent.getFallbackName(""), ParfaitAgent.PARFAIT);
        assertEquals(ParfaitAgent.getFallbackName("12345"), ParfaitAgent.PARFAIT);
        assertEquals(ParfaitAgent.getFallbackName("@localhost"), ParfaitAgent.PARFAIT);
        assertEquals(ParfaitAgent.getFallbackName("12345@localhost"), ParfaitAgent.PARFAIT + "12345");
    }

    @Test
    public void checkDefaultNameResults() {
        assertEquals(ParfaitAgent.getDefaultName(null, null, null), ParfaitAgent.PARFAIT);
        assertEquals(ParfaitAgent.getDefaultName("foo", null, null), "foo");
        assertEquals(ParfaitAgent.getDefaultName("foo", "bar", null), "foo");
        assertEquals(ParfaitAgent.getDefaultName(null, "bar", null), "bar");
        assertEquals(ParfaitAgent.getDefaultName(null, null, "baz"), ParfaitAgent.PARFAIT);
    }
}
