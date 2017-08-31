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
