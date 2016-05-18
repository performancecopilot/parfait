package io.pcp.parfait.benchmark;

import static java.net.InetAddress.getLocalHost;

import java.net.UnknownHostException;

import org.apache.commons.lang.SystemUtils;

public class ReportHelper {
    static void environmentReportHeader()  {
        String hostName = getCurrentHostname();
        System.out.printf("Host: %s\tJava: %s\n", hostName, SystemUtils.JAVA_VERSION);
    }

    private static String getCurrentHostname() {
        String hostName = "UNKNOWN";
        try {
            hostName = getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException ignored) {
        }
        return hostName;
    }
}
