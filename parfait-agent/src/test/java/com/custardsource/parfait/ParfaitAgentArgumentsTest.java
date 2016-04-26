package com.custardsource.parfait;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParfaitAgentArgumentsTest {

    private String originalNameValue;
    private String originalClusterValue;
    private String originalIntervalValue;

    @Before
    public void setup() {
        // setProperty returns the old value of that property.
        originalNameValue = System.setProperty(MonitoringViewProperties.PARFAIT_NAME, "true");
        originalClusterValue = System.setProperty(MonitoringViewProperties.PARFAIT_CLUSTER, "true");
        originalIntervalValue = System.setProperty(MonitoringViewProperties.PARFAIT_INTERVAL, "true");
    }

    private void reset(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    @After
    public void teardown() {
        reset(MonitoringViewProperties.PARFAIT_NAME, originalNameValue);
        reset(MonitoringViewProperties.PARFAIT_CLUSTER, originalClusterValue);
        reset(MonitoringViewProperties.PARFAIT_INTERVAL, originalIntervalValue);
    }

    @Test
    public void checkArgumentName() {
        ParfaitAgent.setupArguments("name:TestApp");
        assertEquals(System.getProperty(MonitoringViewProperties.PARFAIT_NAME), "TestApp");
        System.clearProperty(MonitoringViewProperties.PARFAIT_NAME);
    }

    @Test
    public void checkArgumentCluster() {
        ParfaitAgent.setupArguments("cluster:123");
        assertEquals("123", System.getProperty(MonitoringViewProperties.PARFAIT_CLUSTER));
        System.clearProperty(MonitoringViewProperties.PARFAIT_CLUSTER);
    }

    @Test
    public void checkArgumentNameAndInterval() {
        ParfaitAgent.setupArguments("name:Frodo,interval:20");
        assertEquals("Frodo", System.getProperty(MonitoringViewProperties.PARFAIT_NAME));
        assertEquals("20", System.getProperty(MonitoringViewProperties.PARFAIT_INTERVAL));
        System.clearProperty(MonitoringViewProperties.PARFAIT_INTERVAL);
        System.clearProperty(MonitoringViewProperties.PARFAIT_NAME);
    }
}
