package com.custardsource.parfait;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TimeWindowTest {

	@Test(expected=IllegalArgumentException.class)
	public void constructionShouldRejectZeroPeriod() {
		TimeWindow.of(100, 0, "foo");
	}

	@Test(expected=IllegalArgumentException.class)
	public void constructionShouldRejectZeroResolution() {
		TimeWindow.of(0, 400, "foo");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void constructionShouldRejectMoreThanMaxintWindows() {
		TimeWindow.of(1000, 4000000000000L, "foo");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void constructionShouldRejectPeriodNotMultipleOfResolution() {
		TimeWindow.of(3, 10, "foo");
	}

	@Test
	public void testCalculateBucketCount() {
		assertEquals(100, TimeWindow.of(10, 1000, "foo").getBuckets());
	}

}
