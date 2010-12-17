package com.custardsource.parfait;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.junit.Before;
import org.junit.Test;


public class TimeWindowCounterBuilderTest {
	private MonitorableRegistry registry;
	private MonitoredCounter template;
	private TimeWindowCounterBuilder builder;

	@Before
	public void setUp() {
		registry = new MonitorableRegistry();
		template = new MonitoredCounter("disk.writes", "bytes written to disk", registry,
				NonSI.BYTE);
		builder = new TimeWindowCounterBuilder(registry, TimeWindow.of(1000, 5000, "5s"));
	}

	@Test
	public void metricsShouldBeCreatedWithProvidedValues() {
		builder.build("mails.sent", "emails sent", Unit.ONE);
		assertTrue(registry.containsMetric("mails.sent.5s"));
		assertEquals("emails sent [5s]", registry.getMetric("mails.sent.5s").getDescription());
		assertEquals(Unit.ONE, registry.getMetric("mails.sent.5s").getUnit());
	}

	@Test
	public void metricsShouldBeCreatedWithValuesCopiedFromTemplate() {
		builder.copyFrom(template);
		assertTrue(registry.containsMetric("disk.writes.5s"));
		assertEquals("bytes written to disk [5s]", registry.getMetric("disk.writes.5s").getDescription());
		assertEquals(NonSI.BYTE, registry.getMetric("disk.writes.5s")
				.getUnit());
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
