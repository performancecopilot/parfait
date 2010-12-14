package com.custardsource.parfait;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Supplier;

public class TimeWindowCounterTest {
	private static final long RESOLUTION = 1000L;
	private static final long PERIOD = 3 * RESOLUTION;
	
	private AtomicLong currentTime = new AtomicLong();
	private final Supplier<Long> timeSource = new Supplier<Long>() {
		@Override
		public Long get() {
			return currentTime.get();
		}
	};
	
	@Before
	public void setUp() {
	   currentTime.set(0L);	
	}
	

	@Test(expected=IllegalArgumentException.class)
	public void constructionShouldRejectZeroPeriod() {
		new TimeWindowCounter(100, 0);
	}

	@Test(expected=IllegalArgumentException.class)
	public void constructionShouldRejectZeroResolution() {
		new TimeWindowCounter(0, 400);
	}

	@Test(expected=IllegalArgumentException.class)
	public void constructionShouldRejectPeriodNotMultipleOfResolution() {
		new TimeWindowCounter(3, 10);
	}

	@Test
	public void incrementShouldUpdateValue() {
		TimeWindowCounter counter = new TimeWindowCounter(100, 1000);
		counter.inc();
		assertEquals(1L, counter.get().longValue());
		counter.inc(5);
		assertEquals(6L, counter.get().longValue());
	}

	
	@Test
	public void sameBucketShouldIncrementDuringSameResolution() {
		TimeWindowCounter counter = new TimeWindowCounter(RESOLUTION, PERIOD, timeSource);
		counter.inc();
		assertEquals("[1, 0, 0]", counter.counterState());
		currentTime.addAndGet(RESOLUTION - 1L);
		counter.inc();
		assertEquals("[2, 0, 0]", counter.counterState());
		assertEquals(2L, counter.get().longValue());
	}

	
	@Test
	public void nextBucketShouldIncrementAfterResolutionElapsed() {
		TimeWindowCounter counter = new TimeWindowCounter(RESOLUTION, PERIOD, timeSource);
		counter.inc();
		assertEquals("[1, 0, 0]", counter.counterState());
		currentTime.addAndGet(RESOLUTION);
		counter.inc();
		assertEquals("[1, 1, 0]", counter.counterState());
		assertEquals(2L, counter.get().longValue());
	}

	
	@Test
	public void bucketShouldOverwriteOldValuesAfterPeriod() {
		TimeWindowCounter counter = new TimeWindowCounter(RESOLUTION, PERIOD, timeSource);
		counter.inc(3L);
		assertEquals("[3, 0, 0]", counter.counterState());
		
		currentTime.addAndGet(RESOLUTION * 2);
		counter.inc(4L);
		assertEquals("[3, 0, 4]", counter.counterState());
		
		currentTime.addAndGet(RESOLUTION);
		counter.inc();
		assertEquals("[1, 0, 4]", counter.counterState());
		assertEquals(5L, counter.get().longValue());

		currentTime.addAndGet(RESOLUTION * 3);
		counter.inc(2L);
		assertEquals("[2, 0, 0]", counter.counterState());
		assertEquals(2L, counter.get().longValue());
	}
	
	@Test
	public void getShouldCleanOldValues() {
		TimeWindowCounter counter = new TimeWindowCounter(RESOLUTION, PERIOD, timeSource);
		counter.inc();
		currentTime.addAndGet(PERIOD);
		assertEquals(0L, counter.get().longValue());
	}
	
	@Test
	public void toStringShouldReturnExpectedFormat() {
		TimeWindowCounter counter = new TimeWindowCounter(RESOLUTION, PERIOD, timeSource);
		counter.inc();
		assertEquals("last 3000ms=1", counter.toString());
	}	
}
