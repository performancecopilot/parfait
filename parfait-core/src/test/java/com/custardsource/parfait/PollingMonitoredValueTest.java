package com.custardsource.parfait;

import static org.junit.Assert.assertEquals;


import javax.measure.unit.Unit;

import org.junit.Test;

import com.google.common.base.Supplier;

public class PollingMonitoredValueTest {

	@Test
	public void shouldScheduleTaskAtDesiredRate() {
		TestPoller poller = new TestPoller();
		ManualScheduler scheduler = new ManualScheduler();
		new PollingMonitoredValue<Integer>("polling.test", "",
				new MonitorableRegistry(), 275, poller,
				ValueSemantics.FREE_RUNNING, Unit.ONE, scheduler);
		assertEquals(1, scheduler.scheduledRates.size());
		assertEquals(275, scheduler.scheduledRates.values().iterator().next()
				.intValue());
	}

	@Test
	public void scheduledTaskExecutionShouldUpdateValue()
			throws InterruptedException {
		TestPoller poller = new TestPoller();
		MonitorableRegistry registry = new MonitorableRegistry();
		ManualScheduler scheduler = new ManualScheduler();
		PollingMonitoredValue<Integer> p = new PollingMonitoredValue<Integer>(
				"polling.test", "", registry, 275,
				poller, ValueSemantics.FREE_RUNNING, Unit.ONE, scheduler);
		poller.value = 17;
		assertEquals(0, p.get().intValue());
		scheduler.runAllScheduledTasks();
		assertEquals(17, p.get().intValue());
	}

	private final class TestPoller implements Supplier<Integer> {
		private int value = 0;

		public Integer get() {
			return value;
		}
	}

}
