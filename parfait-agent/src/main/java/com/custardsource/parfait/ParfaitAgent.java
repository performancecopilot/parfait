package com.custardsource.parfait;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.custardsource.parfait.DynamicMonitoringView;
import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.pcp.PcpMonitorBridge;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParfaitAgent {
    private final static Logger logger = LoggerFactory.getLogger(ParfaitAgent.class);
    
    public static Map<String, String> getProperties(String agentArguments) {
        Map<String, String> properties = new HashMap<String, String>();

        if (agentArguments == null) {
            return properties;
        }
        for (String propertyAndValue: agentArguments.split(",")) {
            String[] tokens = propertyAndValue.split(":", 2);
            if (tokens.length == 2) {
                properties.put(tokens[0], tokens[1]);
            }
        }
        return properties;
    }

    public static String defaultName(String runtime) {
        String name = "parfait"; // append PID, inferred from runtime
        String[] pidAndHost = runtime.split("@", 2);

        if (pidAndHost.length == 2) {
            name += pidAndHost[0];
        }
        return name;
    }

    public static void premain(String args, Instrumentation instrumentation) {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List runtimeArguments = runtimeMxBean.getInputArguments();
        String runtimeName = runtimeMxBean.getName();
        String name;

        logger.info("Runtime: {}: {}", runtimeName, runtimeArguments);

        // extract arguments:   name:application[,...]
        Map<String, String> properties = getProperties(args);
        String value = properties.get("name");
        if (value != null) {
            name = value;
        } else {
            name = defaultName(runtimeName);
        }
        System.setProperty("parfait.name", name);
        // TODO: properties hacks to get things bootstrapped (cleanup!)
        System.setProperty("parfait.cluster", "42");
        System.setProperty("parfait.interval", "1000");

        logger.info("Starting Parfait agent {} with arguments {}", name, args);

        // Inject all metrics via parfait-spring and parfait-jmx
        try {
            ApplicationContext context = new ClassPathXmlApplicationContext("java.xml");
            MonitorableRegistry metrics = (MonitorableRegistry)context.getBean("monitorableRegistry");
            PcpMonitorBridge bridge = (PcpMonitorBridge)context.getBean("pcpMonitorBridge");

//          // TODO: addShutdownHook - cleanup mmapped file on orderly shutdown
//          // (extend PcpMmvWriter?  it keeps the path we wish to unlink here)

            DynamicMonitoringView view = new DynamicMonitoringView(metrics, bridge);
            view.start();

        } catch (BeansException e) {
            logger.error("Stopping Parfait agent, cannot setup beans", e);
        }
    }
}
