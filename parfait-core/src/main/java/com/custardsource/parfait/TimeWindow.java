package com.custardsource.parfait;

import net.jcip.annotations.ThreadSafe;

import com.google.common.base.Preconditions;

/**
 * A 'time bucket', used for counters which represent an event count or metric
 * delta over a limited, sliding time window. For example, the number of
 * requests served or bytes written in the last 60 seconds. Takes both a period
 * (the amount of time represented by each window), and a resolution (the
 * duration at which events will be clustered together).
 */
@ThreadSafe
public final class TimeWindow {
	private final int resolution;
	private final long period;
	private final String name;

	private TimeWindow(int resolution, long period, String name) {
		Preconditions.checkArgument(resolution > 0,
				"resolution must be positive");
		Preconditions.checkArgument(period > 0L,
				"period covered must be positive");
		Preconditions.checkArgument(period % resolution == 0,
				"period covered %s must be divisible by resolution %s", period,
				resolution);
		this.resolution = resolution;
		this.period = period;
		this.name = Preconditions.checkNotNull(name);
	}

	public String getName() {
		return name;
	}

	public int getResolution() {
		return resolution;
	}

	public long getPeriod() {
		return period;
	}

	/**
	 * Factory method to create a new TimeWindow.
	 * 
	 * @param resolution
	 *            the fine-grained resolution at which individual events will be
	 *            aggregated. Must be a positive factor of <code>period</code>
	 * @param period
	 *            the duration represented (at least at a
	 *            <code>resolution</code> resolution-level accuracy)
	 * @param name
	 *            a short name for the window; used to name
	 *            automatically-generated metrics based on this window. e.g.
	 *            '1m' or '12h'.
	 */
	public static TimeWindow of(int resolution, long period, String name) {
		return new TimeWindow(resolution, period, name);
	}

	public int getBuckets() {
		return (int) (period / resolution);
	}
}