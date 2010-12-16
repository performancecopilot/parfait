package com.custardsource.parfait;

import org.apache.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Designed to run code after the MonitorableRegistry has become quiet, in terms of addition of new metrics
 */
public class QuiescentRegistryListener implements MonitorableRegistryListener {

    private static final Logger LOG = Logger.getLogger(QuiescentRegistryListener.class);

    private final Timer quiescentTimer = new Timer(getClass().getSimpleName(), true);
    private volatile long lastTimeMonitorableAdded = 0;

    private final Object lock = new Object();


    public QuiescentRegistryListener(final Runnable runnable, final long quietPeriodInMillis) {
        quiescentTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (lock) {
                    if (lastTimeMonitorableAdded > 0 && System.currentTimeMillis() > (lastTimeMonitorableAdded + quietPeriodInMillis)) {
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
        this.lastTimeMonitorableAdded = System.currentTimeMillis();
    }

    public void stop(){
        quiescentTimer.cancel();
    }
}
