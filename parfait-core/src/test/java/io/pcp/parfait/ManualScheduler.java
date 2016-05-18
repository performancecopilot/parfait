package io.pcp.parfait;

import java.util.Map;
import java.util.TimerTask;

import com.google.common.collect.Maps;

final class ManualScheduler implements Scheduler {
	Map<TimerTask, Long> scheduledRates = Maps.newHashMap();

	@Override
	public void schedule(TimerTask task, long rate) {
		scheduledRates.put(task, rate);
	}

	void runAllScheduledTasks() {
		for (TimerTask task : scheduledRates.keySet()) {
			task.run();
		}
	}

	@Override
	public void schedule(TimerTask timerTask, long delay, long rate) {
        // We can safely ignore delay here
		schedule(timerTask, rate);
	}

}