package io.pcp.parfait.benchmark;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;

public class BlockedMetricCollector {

    private final long initialBlockedCount;
    private final long initialBlockedTime;
    private final long threadId;
    private long totalBlockedCount;
    private long totalBlockedTime;

    public BlockedMetricCollector(long threadId) {
        this.threadId = threadId;
        ThreadInfo initialThreadInfo = getThreadInfo(threadId);
        initialBlockedCount = initialThreadInfo.getBlockedCount();
        initialBlockedTime = initialThreadInfo.getBlockedTime();
    }

    public BlockedMetricCollector() {
        this(Thread.currentThread().getId());
    }

    private ThreadInfo getThreadInfo(long threadId) {
        return ManagementFactory.getThreadMXBean().getThreadInfo(threadId);
    }
    
    public void computeFinalValues() {
        ThreadInfo finalThreadInfo = getThreadInfo(threadId);

        long finalBlockedCount = finalThreadInfo.getBlockedCount();
        long finalBlockedTime = finalThreadInfo.getBlockedTime();

        totalBlockedCount = finalBlockedCount - initialBlockedCount;
        totalBlockedTime = finalBlockedTime - initialBlockedTime;
    }

    public long getTotalBlockedCount() {
        return totalBlockedCount;
    }

    public long getTotalBlockedTime() {
        return totalBlockedTime;
    }
}
