package com.custardsource.parfait;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicLong;

import javax.measure.unit.Unit;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Supplier;

public class PeriodicValueTest {
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
		new PeriodicValue("foo", "foo", Unit.ONE, 100, 0);
	}

	@Test(expected=IllegalArgumentException.class)
	public void constructionShouldRejectZeroResolution() {
		new PeriodicValue("foo", "foo", Unit.ONE, 0, 400);
	}

	@Test(expected=IllegalArgumentException.class)
	public void constructionShouldRejectPeriodNotMultipleOfResolution() {
		new PeriodicValue("foo", "foo", Unit.ONE, 3, 10);
	}

	@Test
	public void incrementShouldUpdateValue() {
		PeriodicValue value = new PeriodicValue("foo", "foo", Unit.ONE, 100, 1000);
		value.inc();
		assertEquals(1L, value.get().longValue());
		value.inc(5);
		assertEquals(6L, value.get().longValue());
	}

	
	@Test
	public void sameBucketShouldIncrementDuringSameResolution() {
		PeriodicValue value = new PeriodicValue("foo", "foo", Unit.ONE, RESOLUTION, PERIOD, timeSource);
		value.inc();
		assertEquals("[1, 0, 0]", value.counterState());
		currentTime.addAndGet(RESOLUTION - 1L);
		value.inc();
		assertEquals("[2, 0, 0]", value.counterState());
		assertEquals(2L, value.get().longValue());
	}

	
	@Test
	public void nextBucketShouldIncrementAfterResolutionElapsed() {
		PeriodicValue value = new PeriodicValue("foo", "foo", Unit.ONE, RESOLUTION, PERIOD, timeSource);
		value.inc();
		assertEquals("[1, 0, 0]", value.counterState());
		currentTime.addAndGet(RESOLUTION);
		value.inc();
		assertEquals("[1, 1, 0]", value.counterState());
		assertEquals(2L, value.get().longValue());
	}

	
	@Test
	public void bucketShouldOverwriteOldValuesAfterPeriod() {
		PeriodicValue value = new PeriodicValue("foo", "foo", Unit.ONE, RESOLUTION, PERIOD, timeSource);
		value.inc(3L);
		assertEquals("[3, 0, 0]", value.counterState());
		
		currentTime.addAndGet(RESOLUTION * 2);
		value.inc(4L);
		assertEquals("[3, 0, 4]", value.counterState());
		
		currentTime.addAndGet(RESOLUTION);
		value.inc();
		assertEquals("[1, 0, 4]", value.counterState());
		assertEquals(5L, value.get().longValue());

		currentTime.addAndGet(RESOLUTION * 3);
		value.inc(2L);
		assertEquals("[2, 0, 0]", value.counterState());
		assertEquals(2L, value.get().longValue());
	}
	
	@Test
	public void getShouldCleanOldValues() {
		PeriodicValue value = new PeriodicValue("foo", "foo", Unit.ONE, RESOLUTION, PERIOD, timeSource);
		value.inc();
		currentTime.addAndGet(PERIOD);
		assertEquals(0L, value.get().longValue());
	}
	
	@Test
	public void toStringShouldReturnExpectedFormat() {
		PeriodicValue value = new PeriodicValue("foo", "foo", Unit.ONE, RESOLUTION, PERIOD, timeSource);
		value.inc();
		assertEquals("last 3000ms=1", value.toString());
	}	
}
