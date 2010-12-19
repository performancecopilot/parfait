package com.custardsource.parfait;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class QuiescentRegistryListenerTest {
    private static final int QUIET_PERIOD_IN_SECONDS = 1;
    private static final int MS_PER_SECOND = 1000;

	@Test
	public void schedulerFireShouldUpdateMonitorable() {
		final DelayedRunnableTester delayedRunnableTester = new DelayedRunnableTester();
		final ManualScheduler scheduler = new ManualScheduler();
		final ManualTimeSupplier clock = new ManualTimeSupplier();

		final QuiescentRegistryListener listener = new QuiescentRegistryListener(
				delayedRunnableTester, clock, QUIET_PERIOD_IN_SECONDS
						* MS_PER_SECOND, scheduler);

		final MonitorableRegistry monitorableRegistry = new MonitorableRegistry();
		monitorableRegistry.addRegistryListener(listener);

		final DummyMonitorable dummyMonitorable = new DummyMonitorable("foo");

		clock.setTime(100);
		monitorableRegistry.register(dummyMonitorable);

		clock.tick(QUIET_PERIOD_IN_SECONDS * MS_PER_SECOND);
		scheduler.runAllScheduledTasks();
		assertTrue("Delayed trigger should have fired",
				delayedRunnableTester.runCount() == 1);

		clock.tick(QUIET_PERIOD_IN_SECONDS * MS_PER_SECOND);
		scheduler.runAllScheduledTasks();
		assertTrue(
				"Delayed trigger should not have fired again as no new Monitorable added",
				delayedRunnableTester.runCount() == 1);
	}

    private static class DelayedRunnableTester implements Runnable {
        private int runCount = 0;

        @Override
        public void run() {
            runCount++;
        }

        public int runCount() {
            return runCount;
        }
    }
}
