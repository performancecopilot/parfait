package com.custardsource.parfait;

import java.util.List;

import javax.measure.unit.Unit;

import net.jcip.annotations.ThreadSafe;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@ThreadSafe
public class TimeWindowCounterBuilder {
	private final List<TimeWindow> timeWindows;
	private final Supplier<Long> timeSource;
	private final MonitorableRegistry registry;

	public TimeWindowCounterBuilder(MonitorableRegistry registry,
			TimeWindow... windows) {
		this(TimeWindowCounter.SYSTEM_TIME_SOURCE, registry, windows);
	}

	TimeWindowCounterBuilder(Supplier<Long> timeSource,
			MonitorableRegistry registry, TimeWindow... windows) {
		this.registry = registry;
		this.timeSource = timeSource;
		this.timeWindows = ImmutableList.copyOf(windows);
	}

	public CompositeCounter build(String baseName, String baseDescription,
			Unit<?> unit) {
		List<Counter> counters = getSubCounters(baseName, baseDescription, unit);
		return new CompositeCounter(counters);
	}

	private List<Counter> getSubCounters(String baseName,
			String baseDescription, Unit<?> unit) {
		List<Counter> counters = Lists.newArrayList();
		for (TimeWindow timeWindow : timeWindows) {
			final TimeWindowCounter value = new TimeWindowCounter(timeWindow,
					timeSource);

			new PollingMonitoredValue<Long>(baseName + "."
					+ timeWindow.getName(), baseDescription + " ["
					+ timeWindow.getName() + "]", registry,
					timeWindow.getResolution(), new Poller<Long>() {
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
		return build(templateMonitorable.getName(),
				templateMonitorable.getDescription(),
				templateMonitorable.getUnit());
	}

	public CompositeCounter wrapCounter(MonitoredCounter templateCounter) {
		List<Counter> subCounters = Lists
				.<Counter> newArrayList(templateCounter);
		subCounters.addAll(getSubCounters(templateCounter.getName(),
				templateCounter.getDescription(), templateCounter.getUnit()));
		return new CompositeCounter(subCounters);
	}
}