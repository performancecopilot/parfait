package com.custardsource.parfait;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Designed to run code after the MonitorableRegistry has become quiet, in terms of addition of new metrics
 */
public class QuiescentRegistryListener implements MonitorableRegistryListener {
    private static final Timer quiescentTimer = new Timer(QuiescentRegistryListener.class.getSimpleName(), true);
    private volatile long lastTimeMonitorableAdded = 0;

    private final Object lock = new Object();


    public QuiescentRegistryListener(final Runnable runnable, final long quietPeriodInMillis) {
        quiescentTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (lock) {
                    if (lastTimeMonitorableAdded > 0 && System.currentTimeMillis() > (lastTimeMonitorableAdded + quietPeriodInMillis)) {
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
        this.lastTimeMonitorableAdded = System.currentTimeMillis();
    }
}
