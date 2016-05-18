package io.pcp.parfait;

import java.util.TimerTask;

interface Scheduler {
	public void schedule(TimerTask task, long rate);

	public void schedule(TimerTask timerTask, long delay, long rate);
}