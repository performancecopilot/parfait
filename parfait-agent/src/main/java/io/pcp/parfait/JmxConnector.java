package io.pcp.parfait;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import io.pcp.parfait.DynamicMonitoringView;

//
// JmxConnector class - a Parfait Proxy for JMX.
//
// Runs in different modes
// - linger/daemon mode where it scans for (relatively) short-lived processes
// - one-shot mode for on-demand monitoring of one specific java process
//
// TODO: extend "MonitoredMBeanAttributeFactory" in parfait-jmx to allow
//       MBeanServer injection.  See Spring MBeanClientInterceptor class:
//       http://docs.spring.io/spring-framework/docs/2.0.8/api/org/springframework/jmx/access/MBeanClientInterceptor.html)
//       ... for one possible approach to wiring this up.
// TODO: need a resources/proxy.xml based on resources/agent.xml
// TODO: implement command line option handling (for all options, both modes)
// TODO: update bin/parfait scripts to support these proxying modes too.
//

public class JmxConnector {
    private static final Logger logger = Logger.getLogger(JmxConnector.class);

    public static void setupArguments(String[] arguments) {
        // TODO: extract command line arguments, set local fields / properties for spring
    }

    public static void main(String[] arguments) {
        // extract properties from arguments, properties files, or intuition
        MonitoringViewProperties.setupProperties();
        setupArguments(arguments);

        String name = System.getProperty(MonitoringViewProperties.PARFAIT_NAME);
        logger.debug(String.format("Starting Parfait proxy %s", name));

        // inject all metrics via parfait-spring and parfait-jmx
        try {
            ApplicationContext context = new ClassPathXmlApplicationContext("proxy.xml"); // TODO
            DynamicMonitoringView view = (DynamicMonitoringView)context.getBean("monitoringView");
            view.start();
        } catch (BeansException e) {
            logger.error("Stopping Parfait proxy, cannot setup beans", e);
        }
    }
}
