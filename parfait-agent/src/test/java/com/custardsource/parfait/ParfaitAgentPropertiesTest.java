package com.custardsource.parfait;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ParfaitAgentPropertiesTest {

    private String originalNameValue;
    private String originalClusterValue;
    private String originalIntervalValue;

    @Before
    public void setup() {
        // setProperty returns the old value of that property.
        originalNameValue = System.setProperty(ParfaitAgent.PARFAIT_NAME, "true");
        originalClusterValue = System.setProperty(ParfaitAgent.PARFAIT_CLUSTER, "true");
        originalIntervalValue = System.setProperty(ParfaitAgent.PARFAIT_INTERVAL, "true");
    }

    private void reset(String name, String value) {
        if (originalNameValue == null) {
            System.clearProperty(ParfaitAgent.PARFAIT_NAME);
        } else {
            System.setProperty(ParfaitAgent.PARFAIT_NAME, originalNameValue);
        }
    }

    @After
    public void teardown() {
        reset(ParfaitAgent.PARFAIT_NAME, originalNameValue);
        reset(ParfaitAgent.PARFAIT_CLUSTER, originalClusterValue);
        reset(ParfaitAgent.PARFAIT_INTERVAL, originalIntervalValue);
    }

    @Test
    public void checkArgumentProperties() {
        ParfaitAgent.setupArguments("name:TestApp");
        assertEquals(System.getProperty(ParfaitAgent.PARFAIT_NAME), "TestApp");
        System.clearProperty(ParfaitAgent.PARFAIT_NAME);

        ParfaitAgent.setupArguments("cluster:123");
        assertEquals("123", System.getProperty(ParfaitAgent.PARFAIT_CLUSTER));
        System.clearProperty(ParfaitAgent.PARFAIT_CLUSTER);

        ParfaitAgent.setupArguments("name:Frodo,interval:20");
        assertEquals("Frodo", System.getProperty(ParfaitAgent.PARFAIT_NAME));
        assertEquals("20", System.getProperty(ParfaitAgent.PARFAIT_INTERVAL));
        System.clearProperty(ParfaitAgent.PARFAIT_INTERVAL);
        System.clearProperty(ParfaitAgent.PARFAIT_NAME);
    }

    @Test
    public void checkValidClusterSetting() {
        System.setProperty(ParfaitAgent.PARFAIT_CLUSTER, "123");
        assertEquals("123", ParfaitAgent.getDefaultCluster("anyname"));
    }

    @Test
    public void checkDefaultClusterSetting() {
        System.clearProperty(ParfaitAgent.PARFAIT_CLUSTER);
        assertNotEquals("123", ParfaitAgent.getDefaultCluster("somename"));
    }

    @Test
    public void checkValidIntervalSetting() {
        System.clearProperty(ParfaitAgent.PARFAIT_INTERVAL);
        String interval = ParfaitAgent.getDefaultInterval();
        System.setProperty(ParfaitAgent.PARFAIT_INTERVAL, "bad-do-not-modify");
        assertEquals(interval, ParfaitAgent.getDefaultInterval());
    }

    @Test
    public void checkInvalidIntervalSetting() {
        System.setProperty(ParfaitAgent.PARFAIT_INTERVAL, "13000");
        assertEquals("13000", ParfaitAgent.getDefaultInterval());
        System.clearProperty(ParfaitAgent.PARFAIT_INTERVAL);
    }
}
