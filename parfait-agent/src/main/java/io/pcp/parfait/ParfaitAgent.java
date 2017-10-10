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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.pcp.parfait.MonitoringViewProperties;
import io.pcp.parfait.AgentMonitoringView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.management.MBeanServerConnection;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MBeanServer;

import org.apache.log4j.Logger;

public class ParfaitAgent {
    private static final Logger logger = Logger.getLogger(ParfaitAgent.class);

    private static final String RESOURCE = "/jvm.json";
    private static final String PATHNAME = "/etc/parfait";

    // find the root cause of an exception, for nested BeansException case
    public static Throwable getCause(Throwable e) {
        Throwable cause = null; 
        Throwable result = e;
        while (null != (cause = result.getCause()) && (result != cause)) {
            result = cause;
        }
        return result;
    }

    // extract properties from arguments, properties files, or intuition
    public static void setupProperties(String propertyAndValue, String separator) {
        String[] tokens = propertyAndValue.split(separator, 2);
        if (tokens.length == 2) {
            String name = MonitoringViewProperties.PARFAIT + "." + tokens[0];
            String value = tokens[1];
            System.setProperty(name, value);
        }
    }

    // parse all configuration files from the parfait directory
    public static List<Specification> parseAllSpecifications() {
        List<Specification> allMonitorables = new ArrayList<>();
        try {
            File[] files = new File(PATHNAME).listFiles();
            for (File file : files) {
                allMonitorables.addAll(parseSpecification(file));
            }
        } catch (Exception e) {
            // reported, fallback to specification from resources
        }
        if (allMonitorables.size() < 1) {
            InputStream res = ParfaitAgent.class.getResourceAsStream(RESOURCE);
            allMonitorables.addAll(parseInputSpecification(res));
        }
        return allMonitorables;
    }

    // parse a single configuration file from the parfait directory
    public static List<Specification> parseSpecification(File file) {
        List<Specification> monitorables = new ArrayList<>();
        try {
            InputStream stream = new FileInputStream(file);
            monitorables = parseInputSpecification(stream);
        } catch (Exception e) {
            logger.error(String.format("Ignoring file %s", file.getName()));
        }
        return monitorables;
    }

    // parse a configuration stream (external file or resources)
    public static List<Specification> parseInputSpecification(InputStream in) {
        List<Specification> monitorables = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode metrics = mapper.readTree(in).path("metrics");
            for (JsonNode node : metrics) {
                monitorables.add(new Specification(node));
            }
        } catch (Exception e) {
            logger.error(String.format("Unexpected JSON format\n%s", e.getMessage()));
        }
        return monitorables;
    }

    public static void startView(AgentMonitoringView view) throws InstanceNotFoundException, IntrospectionException, AttributeNotFoundException, UnsupportedOperationException, ReflectionException, MBeanException, IOException {
        List<Specification> allSpecifications;

        allSpecifications = parseAllSpecifications();
        for (Specification specification : allSpecifications) {
            logger.trace(String.format("Registering metric [%s]", specification.getName()));
            view.register(specification);
        }
        view.start();
    }

    public static void startLocal() {
        try {
            MBeanServer server = JmxUtilities.locateMBeanServer();
            AgentMonitoringView view = new AgentMonitoringView(server);
            startView(view);
        } catch (Exception e) {
            String name = MonitoringViewProperties.getName();
            logger.error(String.format("Stopping Parfait agent [%s]", name), e);
        }
    }

    public static void setupPreMainArguments(String arguments) {
        for (String propertyAndValue : arguments.split(",")) {
            setupProperties(propertyAndValue, ":");
        }
    }

    public static void premain(String arguments, Instrumentation instruments) {
        MonitoringViewProperties.setupProperties();
        if (arguments != null) {
            setupPreMainArguments(arguments);
        }
        logger.info(String.format("Starting Parfait agent [%s]",
                    MonitoringViewProperties.getName()));
        startLocal();
    }

    public static void startProxy() {
        String jmx = MonitoringViewProperties.getConnection();
        try {
            MBeanServerConnection server = JmxUtilities.connectMBeanServer(jmx);
            AgentMonitoringView view = new AgentMonitoringView(server);
            startView(view);
            Thread.currentThread().join();    // pause the main proxy thread
        } catch (Exception e) {
            String m = "Stopping Parfait proxy";  // pretty-print some errors
            if (getCause(e) instanceof ConnectException) {
                logger.error(String.format("%s, cannot connect to %s", m, jmx));
            } else if (e instanceof InterruptedException) {
                logger.error(String.format("%s, interrupted", m));
            } else {
                logger.error(String.format("%s, cannot setup beans", m), e);
            }
        }
    }

    public static void setupMainArguments(String[] arguments) {
        for (String propertyAndValue: arguments) {
            if (propertyAndValue.startsWith("-"))
                propertyAndValue = propertyAndValue.substring(1);
            setupProperties(propertyAndValue, "=");
        }
    }

    public static void main(String[] arguments) {
        MonitoringViewProperties.setupProperties();
        setupMainArguments(arguments);
        logger.info(String.format("Starting Parfait proxy [%s %s]",
                    MonitoringViewProperties.getName(),
                    MonitoringViewProperties.getConnection()));
        startProxy();
    }
}
