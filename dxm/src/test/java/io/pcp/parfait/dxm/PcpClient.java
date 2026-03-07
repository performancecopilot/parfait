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

package io.pcp.parfait.dxm;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;

class PcpClient {

    String getMetric(String metricName) throws Exception {
        String pmrepMetricSpec = toPmrepMetricSpec(metricName);
        Process exec = Runtime.getRuntime().exec(new String[]{"pmrep", "-s", "1", "-r", "-H", pmrepMetricSpec});
        exec.waitFor();
        if (exec.exitValue() != 0) {
            throw new PcpFetchException(exec);
        }

        String result = IOUtils.toString(exec.getInputStream(), Charset.defaultCharset()).trim();
        if (result.isEmpty()) {
            throw new PcpFetchException("Could not find metric in the output: " + result);
        }
        return result;
    }

    /**
     * Converts PCP bracket instance syntax (metric[instance]) to pmrep's
     * compact form (metric,,instance). Without this, pmrep doesn't know
     * how to filter by instance name.
     */
    private String toPmrepMetricSpec(String metricName) {
        int bracketStart = metricName.indexOf('[');
        if (bracketStart < 0) {
            return metricName;
        }
        String metric = metricName.substring(0, bracketStart);
        String instance = metricName.substring(bracketStart + 1, metricName.length() - 1);
        return metric + ",," + instance;
    }

    private static class PcpFetchException extends RuntimeException {

        PcpFetchException(Process process) throws IOException {
            super(IOUtils.toString(process.getErrorStream(), Charset.defaultCharset()));
        }

        PcpFetchException(String message) {
            super(message);
        }
    }

}