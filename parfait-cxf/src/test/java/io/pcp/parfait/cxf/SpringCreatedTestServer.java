package io.pcp.parfait.cxf;

import java.net.URISyntaxException;

import org.apache.cxf.testutil.common.AbstractBusTestServerBase;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpringCreatedTestServer extends AbstractBusTestServerBase {

    private org.mortbay.jetty.Server server;

    private static final Logger LOG = LoggerFactory.getLogger(SpringCreatedTestServer.class);

    protected void run() {
        LOG.debug("Starting Server");

        server = new org.mortbay.jetty.Server();

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(9080);
        server.setConnectors(new Connector[] { connector });

        WebAppContext webappcontext = new WebAppContext();
        String contextPath = null;
        try {
            contextPath = getClass().getResource(".").toURI().getPath();
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
        System.out.println(contextPath);
        webappcontext.setContextPath("/api");

        webappcontext.setWar(contextPath);

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] { webappcontext, new DefaultHandler() });

        server.setHandler(handlers);
        try {
            server.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tearDown() throws Exception {
        super.tearDown();
        if (server != null) {
            server.stop();
            server.destroy();
            server = null;
        }
    }

    public static void main(String args[]) {
        try {
            SpringCreatedTestServer s = new SpringCreatedTestServer();
            s.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally {
            LOG.debug("Server setup done!");
        }
    }

}