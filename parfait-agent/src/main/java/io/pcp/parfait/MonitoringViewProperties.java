/*
 * Copyright 2009-2017 Red Hat Inc.
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

package io.pcp.parfait;

import java.lang.management.ManagementFactory;
import java.util.Collections;

import io.pcp.parfait.DynamicMonitoringView;
import io.pcp.parfait.dxm.HashingIdentifierSource;
import io.pcp.parfait.dxm.IdentifierSource;

public class MonitoringViewProperties {
    public static final String PARFAIT = "parfait";

    private static final String NAME = "name";
    private static final String CLUSTER = "cluster";
    private static final String INTERVAL = "interval";
    private static final String STARTUP = "startup";
    private static final String CONNECT = "connect";

    public static final String PARFAIT_NAME = PARFAIT + "." + NAME;
    public static final String PARFAIT_CLUSTER = PARFAIT + "." + CLUSTER;
    public static final String PARFAIT_INTERVAL = PARFAIT + "." + INTERVAL;
    public static final String PARFAIT_STARTUP = PARFAIT + "." + STARTUP;
    public static final String PARFAIT_CONNECT = PARFAIT + "." + CONNECT;

    private static final String DEFAULT_INTERVAL = "1000"; // milliseconds
    private static final String DEFAULT_CONNECT = "localhost:9875";

    public static String getCommandBasename(String command) {
        // trim away arguments, produce a generally sanitized basename
        if (command != null && command.length() > 0) {
            int index;

            if (Character.isLetter(command.charAt(0)) == false)
                return null;
            for (index = 0; index < command.length(); index++) {
                if (Character.isLetterOrDigit(command.charAt(index)) == false)
                    break;
            }
           return command.substring(0, index);
        }
        return null;
    }

    public static String getDefaultCommand() {
        return getCommandBasename(System.getProperty("sun.java.command"));
    }

    public static String getParfaitName() {
        return getCommandBasename(System.getProperty(PARFAIT_NAME));
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

    public static String getDefaultStartup() {
        String startup = System.getProperty(PARFAIT_STARTUP);
        if (startup == null) {
            return Long.toString(DynamicMonitoringView.defaultQuietPeriod());
        }
        try {
            Long.parseLong(startup);    // safe verification with fallback
        } catch (NumberFormatException e) {
            return Long.toString(DynamicMonitoringView.defaultQuietPeriod());
        }
        return startup;
    }

    public static String getDefaultConnection() {
        String connect = System.getProperty(PARFAIT_CONNECT);
        if (connect == null || connect.isEmpty()) {
            return DEFAULT_CONNECT;
        }
        return connect;
    }

    public static void setupProperties() {
        String name = getDefaultName(getParfaitName(), getDefaultCommand(), getRuntimeName());
        System.setProperty(PARFAIT_NAME, name);

        String cluster = getDefaultCluster(name);
        System.setProperty(PARFAIT_CLUSTER, cluster);

        String interval = getDefaultInterval();
        System.setProperty(PARFAIT_INTERVAL, interval);

        String startup = getDefaultStartup();
        System.setProperty(PARFAIT_STARTUP, startup);

        String connect = getDefaultConnection();
        System.setProperty(PARFAIT_CONNECT, connect);
    }

    //
    // Convenience routines for accessing properties after setup
    //
    public static String getName() {
        return System.getProperty(PARFAIT_NAME);
    }
    public static Integer getCluster() {
        return Integer.parseInt(System.getProperty(PARFAIT_CLUSTER));
    }
    public static Long getInterval() {
        return Long.parseLong(System.getProperty(PARFAIT_INTERVAL));
    }
    public static Long getStartup() {
        return Long.parseLong(System.getProperty(PARFAIT_STARTUP));
    }
    public static String getConnection() {
        return System.getProperty(PARFAIT_CONNECT);
    }
}
