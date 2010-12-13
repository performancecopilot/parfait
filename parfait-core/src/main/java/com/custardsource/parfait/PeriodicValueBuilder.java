package com.custardsource.parfait;

import java.util.List;

import javax.measure.unit.Unit;

import net.jcip.annotations.NotThreadSafe;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

@NotThreadSafe
public class PeriodicValueBuilder {
	private final List<Period> periods = Lists.newArrayList();
	private final Supplier<Long> timeSource;
	private final MonitorableRegistry registry;

	public PeriodicValueBuilder(MonitorableRegistry registry) {
		this(PeriodicValue.SYSTEM_TIME_SOURCE, registry);
	}

	PeriodicValueBuilder(Supplier<Long> timeSource, MonitorableRegistry registry) {
		this.registry = registry;
		this.timeSource = timeSource;
	}

	public void addPeriod(Period period) {
		periods.add(period);
	}

	public CompositeCounter build(String baseName, String baseDescription, Unit<?> unit) {
		List<Counter> values = Lists.newArrayList();
		for (Period period : periods) {
			final PeriodicValue value = new PeriodicValue(period.getResolution(), period.getPeriod(),
					timeSource);
			
			new PollingMonitoredValue<Long>(baseName + "."
					+ period.getName(), baseDescription + " [" + period.getName()
					+ "]", registry, period.getResolution(), new Poller<Long>() {
				@Override
				public Long poll() {
					return value.get();
				}
			}, ValueSemantics.FREE_RUNNING, unit);
			
			values.add(value);
		}
		return new CompositeCounter(values);
	}
	
	public CompositeCounter copyFrom(Monitorable<?> templateMonitorable) {
		return build(templateMonitorable.getName(), templateMonitorable.getDescription(),
				templateMonitorable.getUnit());
	}
	
	public static final class Period {
		private final int resolution;
		private final long period;
		private final String name;
		
		private Period(int resolution, long period, String name) {
			this.resolution = resolution;
			this.period = period;
			this.name = name;
		}		

		public String getName() {
			return name;
		}

		public int getResolution() {
			return resolution;
		}

		public long getPeriod() {
			return period;
		}
		
		public static Period of(int resolution, long period, String name) {
			return new Period(resolution, period, name);
		}
	}
}