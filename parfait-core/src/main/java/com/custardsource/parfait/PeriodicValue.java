package com.custardsource.parfait;

import java.util.Arrays;

import javax.measure.unit.Unit;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

import net.jcip.annotations.GuardedBy;

public class PeriodicValue extends AbstractMonitorable<Long> implements Counter {
	static final Supplier<Long> SYSTEM_TIME_SOURCE = new Supplier<Long>() {
		@Override
		public Long get() {
			return System.currentTimeMillis();
		}
	};
	private final long resolution;
	private final long periodCovered;
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

	public PeriodicValue(String name, String description, Unit<?> unit,
			long resolution, long periodCovered) {
		this(name, description, unit, resolution, periodCovered,
				SYSTEM_TIME_SOURCE);
	}

	PeriodicValue(String name, String description, Unit<?> unit,
			long resolution, long periodCovered, Supplier<Long> timeSource) {
		super(name, description, Long.class, unit, ValueSemantics.FREE_RUNNING);
		Preconditions.checkArgument(resolution > 0L,
				"resolution must be positive");
		Preconditions.checkArgument(periodCovered > 0L,
				"period covered must be positive");
		Preconditions.checkArgument(periodCovered % resolution == 0,
				"period covered %s must be divisible by resolution %s",
				periodCovered, resolution);
		this.resolution = resolution;
		this.periodCovered = periodCovered;
		this.interimValues = new long[(int) (periodCovered / resolution)];
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
		long bucketsToSkip = (eventTime - headTime) / resolution;
		while (bucketsToSkip > 0) {
			head = (head + 1) % interimValues.length;
			bucketsToSkip--;
			overallValue -= interimValues[head];
			interimValues[head] = 0L;
			headTime += resolution;
		}
	}

	@Override
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
		return String.format("last %sms=%s", periodCovered, overallValue);
	}
	
	@VisibleForTesting
	String counterState() {
		synchronized (lock) {
			return Arrays.toString(interimValues);
		}
	}
}
