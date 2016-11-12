package io.pcp.parfait.dxm;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PcpClient {

    String getMetric(String metricName) throws Exception {
        Process exec = Runtime.getRuntime().exec("pmdumptext -s 1 -t 0.001 -r " + metricName);
        exec.waitFor();
        if (exec.exitValue() != 0) {
            throw new PcpFetchException(exec);
        }

        String result = IOUtils.toString(exec.getInputStream());
        Pattern pattern = Pattern.compile("\t(.*?)\n");
        Matcher matcher = pattern.matcher(result);
        if(matcher.find()) {
            return matcher.group(1);
        }
        throw new PcpFetchException("Could not find metric in the output: " + result);
    }

    private static class PcpFetchException extends RuntimeException {

        PcpFetchException(Process process) throws IOException {
            super(IOUtils.toString(process.getErrorStream()));
        }

        PcpFetchException(String message) {
            super(message);
        }
    }

}