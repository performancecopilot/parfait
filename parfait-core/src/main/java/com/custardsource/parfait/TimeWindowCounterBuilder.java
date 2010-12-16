package com.custardsource.parfait;

import java.util.List;

import javax.measure.unit.Unit;

import net.jcip.annotations.ThreadSafe;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Factory class to produce multiple sets of {@link TimeWindowCounter
 * TimeWindowCounters} with a specific and consistent set of {@link TimeWindow
 * TimeWindows}, and in turn produce {@link PollingMonitoredValue
 * PollingMonitoredValues} which watch those TimeWindowCounters. Can either
 * create the TimeWindowCounters from scratch, or 'copy' an existing
 * MonitoredCounter.
 */
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

	/**
	 * Builds a new {@link CompositeCounter}, comprised of TimeWindowCounters,
	 * and registers {@link PollingMonitoredValue}s to detect changes in their
	 * values.
	 * 
	 * @param baseName
	 *            the base name of the new MonitoredValues, which will have the
	 *            window name, e.g. ".60s", appended to it for each window
	 * @param baseDescription
	 *            the base description of the new MonitoredValues, to be
	 *            appended with e.g. " [60s]"
	 * @param unit
	 *            the {@link Unit} to use for the new values
	 * @return a CompositeCounter wrapping a set of new TimeWindow-based
	 *         counters
	 */
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

	/**
	 * Creates a new CompositeCounter wrapping TimeWindowCounters (and creating
	 * PollingMonitoredValues), using the supplied Monitorable's name,
	 * description, and unit as the template.
	 * 
	 * @param templateMonitorable
	 *            a Monitorable whose name, description, and unit should be
	 *            copied
	 * @return a CompositeCounter wrapping a set of new TimeWindow-based
	 *         counters
	 */
	public CompositeCounter copyFrom(Monitorable<?> templateMonitorable) {
		return build(templateMonitorable.getName(),
				templateMonitorable.getDescription(),
				templateMonitorable.getUnit());
	}

	/**
	 * Creates a new CompositeCounter wrapping TimeWindowCounters (and creating
	 * PollingMonitoredValues), using the supplied MonitoredCounter's name,
	 * description, and unit as the template. <em>Also</em> wraps the supplied
	 * MonitoredCounter itself (hence providing a single incrementable Counter
	 * which will increment both an overall total and a set of TimeWindow
	 * counters)
	 * 
	 * @param templateCounter
	 *            a Counter whose name, description, and unit should be copied
	 * @return a CompositeCounter wrapping a set of new TimeWindow-based
	 *         counters and the supplied templateCounter
	 */
	public CompositeCounter wrapCounter(MonitoredCounter templateCounter) {
		List<Counter> subCounters = Lists
				.<Counter> newArrayList(templateCounter);
		subCounters.addAll(getSubCounters(templateCounter.getName(),
				templateCounter.getDescription(), templateCounter.getUnit()));
		return new CompositeCounter(subCounters);
	}
}