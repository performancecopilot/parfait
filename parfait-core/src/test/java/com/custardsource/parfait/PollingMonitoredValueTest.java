package com.custardsource.parfait;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.TimerTask;

import javax.measure.unit.Unit;

import org.junit.Test;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

public class PollingMonitoredValueTest {

	@Test
	public void shouldScheduleTaskAtDesiredRate() {
		TestPoller poller = new TestPoller();
		TestScheduler scheduler = new TestScheduler();
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
		TestScheduler scheduler = new TestScheduler();
		PollingMonitoredValue<Integer> p = new PollingMonitoredValue<Integer>(
				"polling.test", "", registry, 275,
				poller, ValueSemantics.FREE_RUNNING, Unit.ONE, scheduler);
		poller.value = 17;
		assertEquals(0, p.get().intValue());
		scheduler.runAllScheduledTasks();
		assertEquals(17, p.get().intValue());
	}

	private final class TestScheduler implements
			PollingMonitoredValue.Scheduler {
		private Map<TimerTask, Integer> scheduledRates = Maps.newHashMap();

		@Override
		public void schedule(TimerTask task, int rate) {
			scheduledRates.put(task, rate);
		}

		private void runAllScheduledTasks() {
			for (TimerTask task : scheduledRates.keySet()) {
				task.run();
			}
		}

	}

	
	private final class TestPoller implements Supplier<Integer> {
		private int value = 0;

		public Integer get() {
			return value;
		}
	}

}
