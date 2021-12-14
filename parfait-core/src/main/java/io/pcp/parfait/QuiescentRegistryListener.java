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

package io.pcp.parfait;

import java.util.Timer;
import java.util.TimerTask;

import com.google.common.base.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Designed to run code after the MonitorableRegistry has become quiet, in terms of addition of new metrics
 */
public class QuiescentRegistryListener implements MonitorableRegistryListener {

    private static final Logger LOG = LoggerFactory.getLogger(QuiescentRegistryListener.class);

    private final Scheduler quiescentScheduler;
    private volatile long lastTimeMonitorableAdded = 0;
    private final Object lock = new Object();
	private final Supplier<Long> clock;

    public QuiescentRegistryListener(final Runnable runnable, final long quietPeriodInMillis) {
    	this (runnable, new SystemTimePoller(), quietPeriodInMillis, new TimerScheduler(new Timer(QuiescentRegistryListener.class.getName(),true)));
    }
    
    QuiescentRegistryListener(final Runnable runnable, final Supplier<Long> clock, final long quietPeriodInMillis, Scheduler scheduler) {
        this.quiescentScheduler = scheduler;
        this.clock = clock;
        quiescentScheduler.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (lock) {
                    if (lastTimeMonitorableAdded > 0 && clock.get().longValue() >= (lastTimeMonitorableAdded + quietPeriodInMillis)) {
                        LOG.info(String.format("New Monitorables detected after quiet period of %dms", quietPeriodInMillis));
                        runnable.run();
                        lastTimeMonitorableAdded = 0;
                    }
                }
            }
        }, 1000, quietPeriodInMillis
        );
    }

    @Override
    public void monitorableAdded(Monitorable<?> monitorable) {
        this.lastTimeMonitorableAdded = clock.get();
    }

    public void stop(){
    }
}
