/*
 * Copyright 2009-2017 Aconex
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

package io.pcp.parfait.cxf;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;

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

    private void hitURL(String fragment) throws MalformedURLException, FileNotFoundException, IOException {
        String endpointAddress = "http://localhost:9080/api/" + fragment;
        URL url = new URL(endpointAddress);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(2000);    // give it up to 2 seconds to respond
        connection.connect();

        // Non-standard ports (9080) can result in an exception from connect.getInputStream() below;
        // in HTTP error cases, HttpURLConnection gives back the input stream via #getErrorStream().
        // 4xx: client error, 5xx: server error. http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html.

        boolean isError = connection.getResponseCode() >= 400;
        InputStream in = isError ? connection.getErrorStream() : connection.getInputStream();
        assertNotNull(in);
    }

}
