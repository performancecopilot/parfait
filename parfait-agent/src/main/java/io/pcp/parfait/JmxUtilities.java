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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;  
import java.util.List;

import javax.management.InstanceNotFoundException;  
import javax.management.MBeanServerConnection;  
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

/**
 * Convenience mechanisms for locating MBeanServer classes.
 */
public abstract class JmxUtilities {

    /**
     * Attempt to find a locally running <code>MBeanServer</code>. Fails if no
     * <code>MBeanServer</code> can be found.  If multiple servers are found,
     * simply returns the first one from the list.
     * @param agent the agent identifier of the MBeanServer to retrieve.
     * If this parameter is <code>null</code>, all registered MBeanServers are
     * considered.
     * @return the <code>MBeanServer</code> if any are found
     * @throws io.pcp.parfait.MBeanServerException
     * if no <code>MBeanServer</code> could be found
     * @see javax.management.MBeanServerFactory#findMBeanServer(String)
     */
    public static MBeanServer locateMBeanServer(String agent) throws MBeanServerException {
        List servers = MBeanServerFactory.findMBeanServer(agent);

        MBeanServer server = null;
        if (servers != null && servers.size() > 0) {
            server = (MBeanServer) servers.get(0);
        }

        if (server == null && agent == null) {
            // Attempt to load the PlatformMBeanServer.
            try {
                server = ManagementFactory.getPlatformMBeanServer();
            }
            catch (SecurityException ex) {
                throw new MBeanServerException("No MBeanServer found, " +
                        "and cannot obtain the Java platform MBeanServer", ex);
            }
        }

        if (server == null) {
            throw new MBeanServerException(
                    "Unable to locate an MBeanServer instance" +
                    (agent != null ? " with agent id [" + agent + "]" : ""));
        }

        return server;
    }

    public static MBeanServer locateMBeanServer() throws MBeanServerException {
        return locateMBeanServer(null);
    }

    /**
     * Attempt to connect to a remote <code>MBeanServer</code>. Fails if no
     * <code>MBeanServer</code> connection can be established.
     */
    public static MBeanServerConnection connectMBeanServer(String server) throws MBeanServerException {
        String url = "service:jmx:rmi://localhost/jndi/rmi://"+server+"/jmxrmi";
        try {
            JMXServiceURL jmxUrl = new JMXServiceURL(url);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxUrl);
            return jmxConnector.getMBeanServerConnection();
        } catch (MalformedURLException e) {
            throw new MBeanServerException(
                    "Problem with JMXServiceURL based on " + url +
                            ": " + e.getMessage());
        } catch (IOException e) {
            throw new MBeanServerException(
                    "Failed to connect to JMX server: " + server +
                            ": " + e.getMessage());
        }  
    }
}
