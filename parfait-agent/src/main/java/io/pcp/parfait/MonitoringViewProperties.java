package io.pcp.parfait;

import java.lang.management.ManagementFactory;
import java.util.Collections;

import io.pcp.parfait.dxm.HashingIdentifierSource;
import io.pcp.parfait.dxm.IdentifierSource;

import org.apache.log4j.Logger;

public class MonitoringViewProperties {
    private static final Logger logger = Logger.getLogger(MonitoringViewProperties.class);

    public static final String PARFAIT = "parfait";

    private static final String NAME = "name";
    private static final String CLUSTER = "cluster";
    private static final String INTERVAL = "interval";

    public static final String PARFAIT_NAME = PARFAIT + "." + NAME;
    public static final String PARFAIT_CLUSTER = PARFAIT + "." + CLUSTER;
    public static final String PARFAIT_INTERVAL = PARFAIT + "." + INTERVAL;

    private static final String DEFAULT_INTERVAL = "1000"; // milliseconds

    public static String getDefaultCommand() {
        return System.getProperty("sun.java.command");
    }

    public static String getParfaitName() {
        return System.getProperty(PARFAIT_NAME);
    }

    public static String getRuntimeName() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }

    public static String getFallbackName(String runtimeName) {
        String name = PARFAIT;

        // check for availability of a parsable runtime string name
        if (runtimeName != null) {
            String[] pidAndHost = runtimeName.split("@", 2);
            if (pidAndHost.length == 2) {
                name += pidAndHost[0];  // append PID, inferred from runtime
            }
        }
        return name;
    }

    public static String getDefaultName(String parfaitName, String commandName, String runtimeName) {
        // check for properties file or command line system property override
        if (parfaitName != null) {
            return parfaitName;
        }
        // check for availability of a user-friendly default command name
        if (commandName != null) {
            return commandName;
        }
        // intuit some other valid name, even if it is just "parfait"
        return getFallbackName(runtimeName);
    }

    public static String getDefaultCluster(String name) {
        String cluster = System.getProperty(PARFAIT_CLUSTER);
        if (cluster == null) {
            IdentifierSource clusterSource = new HashingIdentifierSource(1 << 12);
            Integer id = clusterSource.calculateId(name, Collections.<Integer>emptySet());
            return id.toString();
        }
        return cluster;
    }

    public static String getDefaultInterval() {
        String interval = System.getProperty(PARFAIT_INTERVAL);
        if (interval == null) {
            return DEFAULT_INTERVAL;
        }
        try {
            Integer.parseInt(interval);    // safe verification with fallback
        } catch (NumberFormatException e) {
             return DEFAULT_INTERVAL;
        }
        return interval;
    }

    public static void setupProperties() {
        String name = getDefaultName(getParfaitName(), getDefaultCommand(), getRuntimeName());
        System.setProperty(PARFAIT_NAME, name);

        String cluster = getDefaultCluster(name);
        System.setProperty(PARFAIT_CLUSTER, cluster);

        String interval = getDefaultInterval();
        System.setProperty(PARFAIT_INTERVAL, interval);
    }
}
