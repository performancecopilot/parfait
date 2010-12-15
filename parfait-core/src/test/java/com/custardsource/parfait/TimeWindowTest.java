package com.custardsource.parfait;

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
	public void constructionShouldRejectPeriodNotMultipleOfResolution() {
		TimeWindow.of(3, 10, "foo");
	}
	
}
