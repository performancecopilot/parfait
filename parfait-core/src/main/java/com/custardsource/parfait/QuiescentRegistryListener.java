package com.custardsource.parfait;

import java.util.Timer;
import java.util.TimerTask;

import com.google.common.base.Supplier;
import org.apache.log4j.Logger;

/**
 * Designed to run code after the MonitorableRegistry has become quiet, in terms of addition of new metrics
 */
public class QuiescentRegistryListener implements MonitorableRegistryListener {

    private static final Logger LOG = Logger.getLogger(QuiescentRegistryListener.class);

    private final Timer quiescentTimer = new Timer(getClass().getSimpleName(), true);
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
        quiescentTimer.cancel();
    }
}
