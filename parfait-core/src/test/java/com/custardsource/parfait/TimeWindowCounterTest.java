package com.custardsource.parfait;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


public class TimeWindowCounterTest {
	private static final int RESOLUTION = 1000;
	private static final long PERIOD = 3 * RESOLUTION;
	private static final TimeWindow WINDOW = TimeWindow.of(RESOLUTION, PERIOD, "3s");

	private final TestTimeSupplier timeSource = new TestTimeSupplier();
	private TimeWindowCounter counter;
	
	@Before
	public void setUp() {
	   timeSource.setTime(0L);	
	   counter = new TimeWindowCounter(WINDOW, timeSource);
	}
	

	@Test
	public void incrementShouldUpdateValue() {
		counter.inc();
		assertEquals(1L, counter.get().longValue());
		counter.inc(5);
		assertEquals(6L, counter.get().longValue());
	}

	
	@Test
	public void sameBucketShouldIncrementDuringSameResolution() {
		counter.inc();
		assertEquals("[1, 0, 0]", counter.counterState());
		timeSource.tick(RESOLUTION - 1L);
		counter.inc();
		assertEquals("[2, 0, 0]", counter.counterState());
		assertEquals(2L, counter.get().longValue());
	}

	
	@Test
	public void nextBucketShouldIncrementAfterResolutionElapsed() {
		counter.inc();
		assertEquals("[1, 0, 0]", counter.counterState());
		timeSource.tick(RESOLUTION);
		counter.inc();
		assertEquals("[1, 1, 0]", counter.counterState());
		assertEquals(2L, counter.get().longValue());
	}

	
	@Test
	public void bucketShouldOverwriteOldValuesAfterPeriod() {
		counter.inc(3L);
		assertEquals("[3, 0, 0]", counter.counterState());
		
		timeSource.tick(RESOLUTION * 2);
		counter.inc(4L);
		assertEquals("[3, 0, 4]", counter.counterState());
		
		timeSource.tick(RESOLUTION);
		counter.inc();
		assertEquals("[1, 0, 4]", counter.counterState());
		assertEquals(5L, counter.get().longValue());

		timeSource.tick(RESOLUTION * 3);
		counter.inc(2L);
		assertEquals("[2, 0, 0]", counter.counterState());
		assertEquals(2L, counter.get().longValue());
	}
	
	@Test
	public void getShouldCleanOldValues() {
		counter.inc();
		timeSource.tick(PERIOD);
		assertEquals(0L, counter.get().longValue());
	}
	
	@Test
	public void toStringShouldReturnExpectedFormat() {
		counter.inc();
		assertEquals("last 3s=1", counter.toString());
	}	
}
