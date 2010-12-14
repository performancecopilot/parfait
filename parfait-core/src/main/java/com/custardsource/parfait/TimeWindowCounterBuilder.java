package com.custardsource.parfait;

import java.util.List;

import javax.measure.unit.Unit;

import net.jcip.annotations.NotThreadSafe;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

@NotThreadSafe
public class TimeWindowCounterBuilder {
	private final List<TimeWindow> timeWindows = Lists.newArrayList();
	private final Supplier<Long> timeSource;
	private final MonitorableRegistry registry;

	public TimeWindowCounterBuilder(MonitorableRegistry registry) {
		this(TimeWindowCounter.SYSTEM_TIME_SOURCE, registry);
	}

	TimeWindowCounterBuilder(Supplier<Long> timeSource, MonitorableRegistry registry) {
		this.registry = registry;
		this.timeSource = timeSource;
	}

	public void addWindow(TimeWindow window) {
		timeWindows.add(window);
	}

	public CompositeCounter build(String baseName, String baseDescription, Unit<?> unit) {
		List<Counter> counters = getSubCounters(baseName, baseDescription, unit);
		return new CompositeCounter(counters);
	}

	private List<Counter> getSubCounters(String baseName,
			String baseDescription, Unit<?> unit) {
		List<Counter> counters = Lists.newArrayList();
		for (TimeWindow timeWindow : timeWindows) {
			final TimeWindowCounter value = new TimeWindowCounter(timeWindow.getResolution(), timeWindow.getPeriod(),
					timeSource);
			
			new PollingMonitoredValue<Long>(baseName + "."
					+ timeWindow.getName(), baseDescription + " [" + timeWindow.getName()
					+ "]", registry, timeWindow.getResolution(), new Poller<Long>() {
				@Override
				public Long poll() {
					return value.get();
				}
			}, ValueSemantics.FREE_RUNNING, unit);
			
			counters.add(value);
		}
		return counters;
	}
	
	public CompositeCounter copyFrom(Monitorable<?> templateMonitorable) {
		return build(templateMonitorable.getName(), templateMonitorable.getDescription(),
				templateMonitorable.getUnit());
	}

	public CompositeCounter wrapCounter(MonitoredCounter templateCounter) {
		List<Counter> subCounters = Lists.<Counter>newArrayList(templateCounter);
		subCounters.addAll(getSubCounters(templateCounter.getName(), templateCounter.getDescription(),
				templateCounter.getUnit()));
		return new CompositeCounter(subCounters);
	}
	
	public static final class TimeWindow {
		private final int resolution;
		private final long period;
		private final String name;
		
		private TimeWindow(int resolution, long period, String name) {
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
		
		public static TimeWindow of(int resolution, long period, String name) {
			return new TimeWindow(resolution, period, name);
		}
	}
}