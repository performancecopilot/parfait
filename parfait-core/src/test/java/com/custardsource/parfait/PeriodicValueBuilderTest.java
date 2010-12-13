package com.custardsource.parfait;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.junit.Before;
import org.junit.Test;

import com.custardsource.parfait.PeriodicValueBuilder.Period;

public class PeriodicValueBuilderTest {
	private MonitorableRegistry registry;
	private Monitorable<Long> template;
	private PeriodicValueBuilder builder;

	@Before
	public void setUp() {
		registry = new MonitorableRegistry();
		template = new MonitoredCounter("foo", "bar", registry,
				Unit.ONE.times(1000));
		builder = new PeriodicValueBuilder(registry);
		builder.addPeriod(Period.of(1000, 5000, "5s"));
	}

	@Test
	public void metricsShouldBeCreatedWithProvidedValues() {
		builder.build("baz", "moop", SI.LUX);
		assertTrue(registry.containsMetric("baz.5s"));
		assertEquals("moop [5s]", registry.getMetric("baz.5s").getDescription());
		assertEquals(SI.LUX, registry.getMetric("baz.5s").getUnit());
	}

	@Test
	public void metricsShouldBeCreatedWithValuesCopiedFromTemplate() {
		builder.copyFrom(template);
		assertTrue(registry.containsMetric("foo.5s"));
		assertEquals("bar [5s]", registry.getMetric("foo.5s").getDescription());
		assertEquals(Unit.ONE.times(1000), registry.getMetric("foo.5s")
				.getUnit());
	}

	@Test
	public void wrapCounterShouldProduceNewMetricsWithCopiedValues() {
		MonitoredCounter counter = new MonitoredCounter("plink", "plunk", registry, SI.FARAD);
		builder.wrapCounter(counter);
		assertTrue(registry.containsMetric("plink.5s"));
		assertEquals("plunk [5s]", registry.getMetric("plink.5s").getDescription());
		assertEquals(SI.FARAD, registry.getMetric("plink.5s")
				.getUnit());
	}

	@Test
	public void wrappedCounterShouldIncrementOriginalWhenIncremented() {
		MonitoredCounter counter = new MonitoredCounter("iggle", "piggle", registry, SI.SIEVERT);
		CompositeCounter wrapped = builder.wrapCounter(counter);
		wrapped.inc(23);
		assertEquals(Long.valueOf(23L), counter.get());
		// TODO Should have this assertion, but neet better framework for PollingMonitoredValue
		// assertEquals(Long.valueOf(23L), registry.getMetric("iggle.5s").get());
	}
}
