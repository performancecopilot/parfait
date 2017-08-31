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

import io.pcp.parfait.DynamicMonitoringView;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.net.ConnectException;
import java.util.Arrays;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.support.GenericXmlApplicationContext;

public class ParfaitAgent {
    private static final Logger logger = Logger.getLogger(ParfaitAgent.class);

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

    private static void loadContextProfile(GenericXmlApplicationContext context, String profile) {
        context.getEnvironment().setActiveProfiles(profile);
        context.load("classpath:agent.xml");
        try {
            context.load("file:/etc/parfait/*.xml");
        } catch (Exception e) {
            logger.trace("Cannot setup beans from /etc/parfait", e);
        } 
        context.refresh();
    }

    public static void startLocal() {
        GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        DynamicMonitoringView view;

        try {
            loadContextProfile(context, "local");
            view = (DynamicMonitoringView)context.getBean("monitoringView");
            view.start();
        } catch (BeansException e) {
            logger.error("Stopping Parfait agent, cannot setup beans", e);
        } finally {
            context.close();
        }
    }

    public static void setupPreMainArguments(String arguments) {
        for (String propertyAndValue: arguments.split(",")) {
            setupProperties(propertyAndValue, ":");
        }
    }

    public static void premain(String arguments, Instrumentation instruments) {
        MonitoringViewProperties.setupProperties();
        if (arguments != null) {
            setupPreMainArguments(arguments);
        }
        String name = System.getProperty(MonitoringViewProperties.PARFAIT_NAME);
        logger.info(String.format("Starting Parfait agent [%s]", name));
        startLocal();
    }

    public static void startProxy(String jmx) {
        DynamicMonitoringView view;
        GenericXmlApplicationContext context = new GenericXmlApplicationContext();

        try {
            loadContextProfile(context, "proxy");
            view = (DynamicMonitoringView)context.getBean("monitoringView");
            view.start();
            Thread.currentThread().join();    // pause the main proxy thread
        } catch (Exception e) {
            String m = "Stopping Parfait proxy";  // pretty-print some errors
            if (getCause(e) instanceof ConnectException) {
                logger.error(String.format("%s, cannot connect to %s", m, jmx));
            } else if (e instanceof BeansException) {
                logger.error(String.format("%s, cannot setup beans", m), e);
            } else if (e instanceof InterruptedException) {
                logger.error(String.format("%s, interrupted", m));
            } else {
                logger.error(m, e);
            }
        } finally {
            context.close();
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
        String name = System.getProperty(MonitoringViewProperties.PARFAIT_NAME);
        String c = System.getProperty(MonitoringViewProperties.PARFAIT_CONNECT);
        logger.info(String.format("Starting Parfait proxy [%s %s]", name, c));
        startProxy(c);
    }
}
