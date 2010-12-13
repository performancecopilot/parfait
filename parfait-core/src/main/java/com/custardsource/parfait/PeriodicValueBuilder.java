package com.custardsource.parfait;

import java.util.List;

import net.jcip.annotations.NotThreadSafe;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

@NotThreadSafe
public class PeriodicValueBuilder {
	private final List<PeriodicValue> values = Lists.newArrayList();
	private final Supplier<Long> timeSource;
	private final Monitorable<Long> templateMonitorable;
	private int updateInterval = 1000;
	private final MonitorableRegistry registry;

	public PeriodicValueBuilder(Monitorable<Long> templateMonitorable,
			MonitorableRegistry registry) {
		this(templateMonitorable, PeriodicValue.SYSTEM_TIME_SOURCE, registry);
	}

	PeriodicValueBuilder(Monitorable<Long> templateMonitorable,
			Supplier<Long> timeSource, MonitorableRegistry registry) {
		this.templateMonitorable = templateMonitorable;
		this.registry = registry;
		this.timeSource = timeSource;
	}

	public PeriodicValueBuilder withUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
		return this;
	}

	public void addPeriod(long resolution, long period, String name) {
		final PeriodicValue value = new PeriodicValue(resolution, period,
				timeSource);

		new PollingMonitoredValue<Long>(templateMonitorable.getName() + "."
				+ name, templateMonitorable.getDescription() + " [" + name
				+ "]", registry, updateInterval, new Poller<Long>() {
			@Override
			public Long poll() {
				return value.get();
			}
		}, ValueSemantics.FREE_RUNNING, templateMonitorable.getUnit());

		values.add(value);
	}

	public CompositeCounter build() {
		return new CompositeCounter(values);
	}
}