package com.custardsource.parfait.cxf;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.cxf.testutil.common.AbstractTestServerBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ParfaitIntegrationTest {
    private AbstractTestServerBase testServer;

	@Before
    public void startServer() throws Exception {
    	testServer = new SpringCreatedTestServer();
    	testServer.startInProcess();
    }

	@After
    public void stopServer() throws Exception {
    	testServer.tearDown();
    }

    @Test
    public void testInterceptor() throws Exception {
    	hitURL("restdemo/sayhello");
    	hitURL("restdemo/snooze");
    }

	private void hitURL(String fragment) throws MalformedURLException, IOException {
		String endpointAddress = "http://localhost:9080/api/" + fragment;
        URL url = new URL(endpointAddress);
        URLConnection connect = url.openConnection();
        InputStream in = connect.getInputStream();
        assertNotNull(in);
	}

}