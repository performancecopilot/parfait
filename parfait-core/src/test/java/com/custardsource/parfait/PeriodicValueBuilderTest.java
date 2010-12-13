package com.custardsource.parfait;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.measure.unit.Unit;

import org.junit.Before;
import org.junit.Test;

public class PeriodicValueBuilderTest {
	private MonitorableRegistry registry;
	private Monitorable<Long> template;

	@Before
	public void setUp() {
		registry = new MonitorableRegistry();
		template = new MonitoredCounter("foo", "bar", registry,
				Unit.ONE.times(1000));
		PeriodicValueBuilder builder = new PeriodicValueBuilder(template,
				registry);
		builder.addPeriod(1000, 5000, "5s");
	}

	@Test
	public void metricsShouldUseTemplateNameWithSuffix() {
		assertTrue(registry.containsMetric("foo.5s"));
	}

	@Test
	public void metricsShouldCopyUnitFromTemplate() {
		assertEquals(Unit.ONE.times(1000), registry.getMetric("foo.5s")
				.getUnit());
	}

	@Test
	public void metricsShouldUseTemplateDescriptionWithSuffix() {
		assertEquals("bar [5s]", registry.getMetric("foo.5s").getDescription());
	}
}
