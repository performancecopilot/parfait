package io.pcp.parfait.dxm;

import org.junit.Test;

import static org.junit.Assert.*;

public class MetricNameTest {
	@Test(expected = IllegalArgumentException.class)
	public void testCannotParseWithTrailingDot() {
		MetricName.parse("a..");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCannotParseWithDoubleDot() {
		MetricName.parse("a..b");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCannotParseWithLeadingDot() {
		MetricName.parse(".a");
	}

	@Test
	public void testCanParseWithSingleSegment() {
		assertMetricNameMatches(MetricName.parse("a"), "a", null, "a");
	}

	@Test
	public void testCanParseWithMultipleSegments() {
		assertMetricNameMatches(MetricName.parse("a.b"), "a.b", null, "a.b");
	}

	@Test
	public void testCanParseWithInstance() {
		assertMetricNameMatches(MetricName.parse("a[x].b"), "a.b", "x", "a");
	}

	@Test
	public void testCanParseWithInstanceAtEnd() {
		assertMetricNameMatches(MetricName.parse("a.b[x]"), "a.b", "x", "a.b");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCannotParseWithTwoInstances() {
		MetricName.parse("a[x].b[x]");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCannotParseWithHalfAnInstance() {
		MetricName.parse("a[x.b");
	}

	@Test
	public void testCanParseWithUnderscores() {
		assertMetricNameMatches(MetricName.parse("a._b_"), "a._b_", null, "a._b_");
	}

	private void assertMetricNameMatches(MetricName parsedName, String expectedMetric,
			String expectedInstance, String expectedInstanceDomain) {
		assertEquals(expectedMetric, parsedName.getMetric());
		assertEquals(expectedInstance, parsedName.getInstance());
		assertEquals(expectedInstanceDomain, parsedName.getInstanceDomainTag());
	}

}
