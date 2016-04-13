package com.custardsource.parfait;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.custardsource.parfait.DynamicMonitoringView;
import com.custardsource.parfait.dxm.HashingIdentifierSource;
import com.custardsource.parfait.dxm.IdentifierSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.apache.log4j.Logger;

public class ParfaitAgent {
    private static final Logger logger = Logger.getLogger(ParfaitAgent.class);

    private static final String PARFAIT = "parfait";

    private static final String NAME = "name";
    private static final String CLUSTER = "cluster";
    private static final String INTERVAL = "interval";

    private static final String PARFAIT_NAME = PARFAIT + "." + NAME;
    private static final String PARFAIT_CLUSTER = PARFAIT + "." + CLUSTER;
    private static final String PARFAIT_INTERVAL = PARFAIT + "." + INTERVAL;

    private static final String DEFAULT_INTERVAL = "1000"; // milliseconds

    public static String getDefaultCommand() {
        return System.getProperty("sun.java.command");
    }

    public static String getParfaitName() {
        return System.getProperty("parfait.name");
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
        return interval;
    }

    public static void setupProperties() {
        String name = getDefaultName(getParfaitName(), getDefaultCommand(), getRuntimeName());
        System.setProperty("parfait.name", name);

        String cluster = getDefaultCluster(name);
        System.setProperty("parfait.cluster", cluster);

        String interval = getDefaultInterval();
        System.setProperty("parfait.interval", interval);
    }

    public static void setupArguments(String arguments) {
        for (String propertyAndValue: arguments.split(",")) {
            String[] tokens = propertyAndValue.split(":", 2);
            if (tokens.length == 2) {
                String name = PARFAIT + "." + tokens[0];
                String value = tokens[1];
                System.setProperty(name, value);
            }
        }
    }

    public static void premain(String arguments, Instrumentation instrumentation) {
        String runtimeName = ManagementFactory.getRuntimeMXBean().getName();

        logger.debug(String.format("Runtime: %s [%s]", runtimeName, arguments));

        // extract properties from arguments, properties files, or intuition
        setupProperties();
        if (arguments != null) {
            setupArguments(arguments);
        }

        String name = System.getProperty(PARFAIT_NAME);
        logger.debug(String.format("Starting Parfait agent %s", name));

        // inject all metrics via parfait-spring and parfait-jmx
        try {
            ApplicationContext context = new ClassPathXmlApplicationContext("java.xml");
            DynamicMonitoringView view = (DynamicMonitoringView)context.getBean("monitoringView");
            view.start();
        } catch (BeansException e) {
            logger.error("Stopping Parfait agent, cannot setup beans", e);
        }
    }
}
