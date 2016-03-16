package com.custardsource.parfait;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.unit.Unit;

import com.custardsource.parfait.DynamicMonitoringView;
import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.pcp.PcpMonitorBridge;
import com.custardsource.parfait.dxm.IdentifierSourceSet;
import com.custardsource.parfait.dxm.PcpMmvWriter;
import com.custardsource.parfait.dxm.PcpMmvWriter.MmvFlag;
import com.custardsource.parfait.dxm.MetricName;
import com.custardsource.parfait.dxm.semantics.Semantics;

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

        logger.info("Starting Parfait agent {} with arguments {}", name, args);

         
        // Inject all metrics via parfait-spring and parfait-jmx
        ApplicationContext context = new ClassPathXmlApplicationContext("monitoring.xml");
        PcpMonitorBridge monitor = (PcpMonitorBridge) context.getBean("pcpMonitorBridge");
        if (monitor == null) {
            logger.info("Parfait agent has no spring bean goodness");
        } else {
            logger.info("Parfait agent got some magic beans!!!");
        }


        //
        // Add sample metrics (for prototyping, just do this "by hand" for now)
        //

        PcpMmvWriter bridge = new PcpMmvWriter(name, IdentifierSourceSet.DEFAULT_SET);

        // we'll keep the prefix (remove noprefix flag) and monitor the PID
        bridge.setFlags(EnumSet.of(MmvFlag.MMV_FLAG_PROCESS));


        // Automatically uses default int handler
        bridge.addMetric(MetricName.parse("sheep[baabaablack].bagsfull.count"), Semantics.COUNTER, Unit.ONE.times(1000), 3);

        // Automatically uses default boolean-to-int handler
        bridge.addMetric(MetricName.parse("sheep[baabaablack].bagsfull.haveany"), Semantics.INSTANT, null, new AtomicBoolean(true));

        bridge.addMetric(MetricName.parse("sheep[limpy].bagsfull.haveany"), Semantics.INSTANT, null, new AtomicBoolean(false));

        // Automatically uses default long handler
        bridge.addMetric(MetricName.parse("sheep[insomniac].jumps"), Semantics.COUNTER, Unit.ONE, 12345678901234L);

        // Set up some help text
        bridge.setInstanceDomainHelpText("sheep", "sheep in the paddock",
                "List of all the sheep in the paddock. Includes 'baabaablack', 'insomniac' (who likes to jump fences), and 'limpy' the three-legged wonder sheep.");
        bridge.setMetricHelpText("sheep.jumps", "# of jumps done",
                "Number of times the sheep has jumped over its jumpitem");

        // TODO: addShutdownHook - cleanup mmapped file on orderly shutdown
        // (extend PcpMmvWriter? - it has the path to unlink)

        // All the metrics are added; write the file
        try {
            bridge.start();
            // Metrics are visible to the agent from this point on

            // Sold a bag! Better update the count
            bridge.updateMetric(MetricName.parse("sheep[baabaablack].bagsfull.count"), 2);
            // Values will be reflected in the agent immediately
        } catch (IOException ioError) {
           logger.info("Stopping Parfait agent, error observed\n" + ioError);
        }
    }
}
