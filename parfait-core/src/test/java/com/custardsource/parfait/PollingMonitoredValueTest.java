package com.custardsource.parfait;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

public class PollingMonitoredValueTest extends TestCase {

    public void testDoesCallThePollerAtCorrectInterval() throws InterruptedException {
        TestPoller poller = new TestPoller();
        PollingMonitoredValue<Integer> p = new PollingMonitoredValue<Integer>("polling.test", "",
                MonitorableRegistry.DEFAULT_REGISTRY, 275, poller, ValueSemantics.FREE_RUNNING);
        poller.count.tryAcquire(6, 10, TimeUnit.SECONDS);

        // Need to sleep just a tiny bit to let the poller update it's current value.
        Thread.sleep(50);
        assertEquals(6, (int) p.get());
        assertTrue(poller.averagePollInterval > 250 && poller.averagePollInterval < 300);
    }

    private final class TestPoller implements Poller<Integer> {
        private long averagePollInterval = 250;

        private long lastPollTime = System.currentTimeMillis();

        private final Semaphore count = new Semaphore(0);

        public Integer poll() {
            long now = System.currentTimeMillis();
            averagePollInterval = ((now - lastPollTime) + averagePollInterval) / 2;
            lastPollTime = now;
            int permits = count.availablePermits();
            count.release();
            return permits + 1;
        }
    }

}
