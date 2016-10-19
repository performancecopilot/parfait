package io.pcp.parfait;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParfaitAgentArgumentsTest {

    private String originalNameValue;
    private String originalClusterValue;
    private String originalIntervalValue;
    private String originalStartupValue;

    @Before
    public void setup() {
        // setProperty returns the old value of that property.
        originalNameValue = System.setProperty(MonitoringViewProperties.PARFAIT_NAME, "true");
        originalClusterValue = System.setProperty(MonitoringViewProperties.PARFAIT_CLUSTER, "true");
        originalIntervalValue = System.setProperty(MonitoringViewProperties.PARFAIT_INTERVAL, "true");
        originalStartupValue = System.setProperty(MonitoringViewProperties.PARFAIT_STARTUP, "true");
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
        reset(MonitoringViewProperties.PARFAIT_STARTUP, originalStartupValue);
    }

    @Test
    public void checkArgumentName() {
        ParfaitAgent.setupPreMainArguments("name:TestApp");
        assertEquals(System.getProperty(MonitoringViewProperties.PARFAIT_NAME), "TestApp");
        System.clearProperty(MonitoringViewProperties.PARFAIT_NAME);
    }

    @Test
    public void checkArgumentCluster() {
        ParfaitAgent.setupPreMainArguments("cluster:123");
        assertEquals("123", System.getProperty(MonitoringViewProperties.PARFAIT_CLUSTER));
        System.clearProperty(MonitoringViewProperties.PARFAIT_CLUSTER);
    }

    @Test
    public void checkArgumentNameAndInterval() {
        ParfaitAgent.setupPreMainArguments("name:Frodo,interval:20");
        assertEquals("Frodo", System.getProperty(MonitoringViewProperties.PARFAIT_NAME));
        assertEquals("20", System.getProperty(MonitoringViewProperties.PARFAIT_INTERVAL));
        System.clearProperty(MonitoringViewProperties.PARFAIT_INTERVAL);
        System.clearProperty(MonitoringViewProperties.PARFAIT_NAME);
    }

    @Test
    public void checkArgumentStartup() {
        ParfaitAgent.setupPreMainArguments("startup:12345");
        assertEquals("12345", System.getProperty(MonitoringViewProperties.PARFAIT_STARTUP));
        System.clearProperty(MonitoringViewProperties.PARFAIT_STARTUP);
    }
}
