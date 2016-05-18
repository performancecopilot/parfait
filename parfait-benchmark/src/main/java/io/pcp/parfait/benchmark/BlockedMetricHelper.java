package io.pcp.parfait.benchmark;

import java.util.List;

public class BlockedMetricHelper {
    private BlockedMetricHelper() {
    }

    static long computeTotalBlockedCount(List<BlockedMetricCollector> blockedMetricCollectors) {
        long totalBlockedCount = 0;
        for (BlockedMetricCollector collector : blockedMetricCollectors) {
            totalBlockedCount += collector.getTotalBlockedCount();
        }
        return totalBlockedCount;
    }

    static long computeTotalBlockedTime(List<BlockedMetricCollector> blockedMetricCollectors) {
        long totalBlockedTime = 0;
        for (BlockedMetricCollector collector : blockedMetricCollectors) {
            totalBlockedTime += collector.getTotalBlockedTime();
        }
        return totalBlockedTime;
    }
}