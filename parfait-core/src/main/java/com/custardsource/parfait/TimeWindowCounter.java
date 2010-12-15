package com.custardsource.parfait;

import java.util.Arrays;

import net.jcip.annotations.GuardedBy;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;

public class TimeWindowCounter implements Counter {
	static final Supplier<Long> SYSTEM_TIME_SOURCE = new Supplier<Long>() {
		@Override
		public Long get() {
			return System.currentTimeMillis();
		}
	};
	@GuardedBy("lock")
	private long overallValue;
	@GuardedBy("lock")
	private final long[] interimValues;
	@GuardedBy("lock")
	private int head = 0;
	@GuardedBy("lock")
	private long headTime;

	private final Object lock = new Object();
	private final Supplier<Long> timeSource;
	private final TimeWindow window;

	public TimeWindowCounter(TimeWindow window) {
		this(window, SYSTEM_TIME_SOURCE);
	}

	TimeWindowCounter(TimeWindow window, Supplier<Long> timeSource) {
		this.window = window;
		this.interimValues = new long[(int) (window.getBuckets())];
		this.timeSource = timeSource;
		this.headTime = timeSource.get();
	}

	@Override
	public void inc(long increment) {
		synchronized (lock) {
			cleanState();
			overallValue += increment;
			interimValues[head] += increment;
		}
	}

	@GuardedBy("lock")
	private void cleanState() {
		long eventTime = timeSource.get();
		long bucketsToSkip = (eventTime - headTime) / window.getResolution();
		while (bucketsToSkip > 0) {
			head = (head + 1) % interimValues.length;
			bucketsToSkip--;
			overallValue -= interimValues[head];
			interimValues[head] = 0L;
			headTime += window.getResolution();
		}
	}

	public Long get() {
		synchronized (lock) {
			cleanState();
			return overallValue;
		}
	}

	@Override
	public void inc() {
		inc(1L);
	}

	@Override
	public String toString() {
		return String.format("last %s=%s", window.getName(), overallValue);
	}
	
	@VisibleForTesting
	String counterState() {
		synchronized (lock) {
			return Arrays.toString(interimValues);
		}
	}
}
