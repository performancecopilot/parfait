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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PcpClient {

    String getMetric(String metricName) throws Exception {
        Process exec = Runtime.getRuntime().exec("pmdumptext -s 1 -t 0.001 -r " + metricName);
        exec.waitFor();
        if (exec.exitValue() != 0) {
            throw new PcpFetchException(exec);
        }

        String result = IOUtils.toString(exec.getInputStream(), Charset.defaultCharset());
        Pattern pattern = Pattern.compile("\t(.*?)\n");
        Matcher matcher = pattern.matcher(result);
        if(matcher.find()) {
            return matcher.group(1);
        }
        throw new PcpFetchException("Could not find metric in the output: " + result);
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