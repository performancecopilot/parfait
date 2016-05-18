package io.pcp.parfait;

import java.util.Timer;
import java.util.TimerTask;

public class TimerScheduler implements Scheduler {
	private final Timer timer;

	public TimerScheduler(Timer timer) {
		this.timer = timer;
	}

	@Override
	public void schedule(TimerTask task, long rate) {
		schedule(task, rate, rate);

	}

	@Override
	public void schedule(TimerTask timerTask, long delay, long rate) {
		timer.scheduleAtFixedRate(timerTask, delay, rate);
	}

}
