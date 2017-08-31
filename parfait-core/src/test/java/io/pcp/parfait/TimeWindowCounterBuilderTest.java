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
import static org.junit.Assert.assertTrue;
import static tec.uom.se.AbstractUnit.ONE;
import static systems.uom.unicode.CLDR.BYTE;

import org.junit.Before;
import org.junit.Test;


public class TimeWindowCounterBuilderTest {
	private MonitorableRegistry registry;
	private MonitoredCounter template;
	private TimeWindowCounterBuilder builder;

	@Before
	public void setUp() {
		registry = new MonitorableRegistry();
		template = new MonitoredCounter("disk.writes", "bytes written to disk", registry, BYTE);
		builder = new TimeWindowCounterBuilder(registry, TimeWindow.of(1000, 5000, "5s"));
	}

	@Test
	public void metricsShouldBeCreatedWithProvidedValues() {
		builder.build("mails.sent", "emails sent", ONE);
		assertTrue(registry.containsMetric("mails.sent.5s"));
		assertEquals("emails sent [5s]", registry.getMetric("mails.sent.5s").getDescription());
		assertEquals(ONE, registry.getMetric("mails.sent.5s").getUnit());
	}

	@Test
	public void metricsShouldBeCreatedWithValuesCopiedFromTemplate() {
		builder.copyFrom(template);
		assertTrue(registry.containsMetric("disk.writes.5s"));
		assertEquals("bytes written to disk [5s]", registry.getMetric("disk.writes.5s").getDescription());
		assertEquals(BYTE, registry.getMetric("disk.writes.5s").getUnit());
	}

	@Test
	public void wrappedCounterShouldIncrementOriginalWhenIncremented() {
		CompositeCounter wrapped = builder.wrapCounter(template);
		wrapped.inc(23);
		assertEquals(Long.valueOf(23L), template.get());
		PollingMonitoredValue.runAllTasks();
		assertEquals(Long.valueOf(23L), registry.getMetric("disk.writes.5s").get());
	}
}
