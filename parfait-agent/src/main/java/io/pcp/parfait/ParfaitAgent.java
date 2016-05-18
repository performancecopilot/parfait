package io.pcp.parfait;

import io.pcp.parfait.DynamicMonitoringView;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ParfaitAgent {
    private static final Logger logger = Logger.getLogger(ParfaitAgent.class);

    public static void setupArguments(String arguments) {
        for (String propertyAndValue: arguments.split(",")) {
            String[] tokens = propertyAndValue.split(":", 2);
            if (tokens.length == 2) {
                String name = MonitoringViewProperties.PARFAIT + "." + tokens[0];
                String value = tokens[1];
                System.setProperty(name, value);
            }
        }
    }

    public static void premain(String arguments, Instrumentation instrumentation) {
        String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
        logger.debug(String.format("Agent runtime: %s [%s]", runtimeName, arguments));

        // extract properties from arguments, properties files, or intuition
        MonitoringViewProperties.setupProperties();
        if (arguments != null) {
            setupArguments(arguments);
        }

        String name = System.getProperty(MonitoringViewProperties.PARFAIT_NAME);
        logger.debug(String.format("Starting Parfait agent %s", name));

        // inject all metrics via parfait-spring and parfait-jmx
        try {
            ApplicationContext context = new ClassPathXmlApplicationContext("agent.xml");
            DynamicMonitoringView view = (DynamicMonitoringView)context.getBean("monitoringView");
            view.start();
        } catch (BeansException e) {
            logger.error("Stopping Parfait agent, cannot setup beans", e);
        }
    }
}
