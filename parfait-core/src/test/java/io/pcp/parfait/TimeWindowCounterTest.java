/*
 * Copyright 2009-2017 Aconex
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.pcp.parfait;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


public class TimeWindowCounterTest {
	private static final int RESOLUTION = 1000;
	private static final long PERIOD = 3 * RESOLUTION;
	private static final TimeWindow WINDOW = TimeWindow.of(RESOLUTION, PERIOD, "3s");

	private final ManualTimeSupplier timeSource = new ManualTimeSupplier();
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
