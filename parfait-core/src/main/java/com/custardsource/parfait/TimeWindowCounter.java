package com.custardsource.parfait;

import java.util.Arrays;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;

/**
 * <p>
 * Counter which keeps track of the increments only over a particular
 * {@link TimeWindow}, expiring old increments after the window has elapsed. For
 * example, a period of Uses the resolution of the supplied TimeWindow to group
 * events together for efficiency. For example, when supplied with a TimeWindow
 * with a period of 60 seconds and a resolution of 5 seconds, 12 'buckets' will
 * be created to keep count of events. This means that as each bucket is
 * overwritten after 60 seconds, the total count may be understated by as much
 * as (5/60) ≅ 8%. This, however, means that the memory footprint is
 * approximately that of just the 12 counters, rather than having to track the
 * time of each individual event.
 * </p>
 * <p>
 * Currently uses very coarse-grained locking (each {@link #get()} or
 * {@link #inc(long)} takes and holds a shared lock for the duration); this may
 * prove too contentious and require change later.
 * </p>
 */
@ThreadSafe
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
	private int headIndex = 0;
	@GuardedBy("lock")
	private long headTime;

	private final Object lock = new Object();
	private final Supplier<Long> timeSource;
	private final TimeWindow window;

	public TimeWindowCounter(TimeWindow window) {
		this(window, SYSTEM_TIME_SOURCE);
	}

	@VisibleForTesting
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
			interimValues[headIndex] += increment;
		}
	}

	/**
	 * Clean out old data from the buckets, getting us ready to enter a new
	 * bucket. interimValues, headTime, and headIndex comprise a circular buffer
	 * of the last n sub-values, and the start time of the head bucket. On each
	 * write or get, we progressively clear out entries in the circular buffer
	 * until headTime is within one 'tick' of the current time; we have then
	 * found the correct bucket.
	 */
	@GuardedBy("lock")
	private void cleanState() {
		long eventTime = timeSource.get();
		long bucketsToSkip = (eventTime - headTime) / window.getResolution();
		while (bucketsToSkip > 0) {
			headIndex = (headIndex + 1) % interimValues.length;
			bucketsToSkip--;
			overallValue -= interimValues[headIndex];
			interimValues[headIndex] = 0L;
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
