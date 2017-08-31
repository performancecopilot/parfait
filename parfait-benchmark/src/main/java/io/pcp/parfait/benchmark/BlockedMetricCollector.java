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
