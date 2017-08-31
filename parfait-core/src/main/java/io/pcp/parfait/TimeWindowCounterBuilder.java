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

import java.util.List;

import javax.measure.Unit;

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
		this(new SystemTimePoller(), registry, windows);
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

			String name = String
					.format("%s.%s", baseName, timeWindow.getName());
			String description = String.format("%s [%s]", baseDescription,
					timeWindow.getName());
			PollingMonitoredValue.poll(name, description, registry,
					timeWindow.getResolution(), new Supplier<Long>() {
						@Override
						public Long get() {
							return value.get();
						}
					}, ValueSemantics.FREE_RUNNING, unit);

			counters.add(value);
		}
		return counters;
	}

	/**
	 * Creates a new CompositeCounter wrapping TimeWindowCounters (and creating
	 * PollingMonitoredValues), using the supplied counter's name,
	 * description, and unit as the template.
	 * 
	 * @param templateCounter
	 *            a MonitoredCounter whose name, description, and unit should be
	 *            copied
	 * @return a CompositeCounter wrapping a set of new TimeWindow-based
	 *         counters
	 */
	public CompositeCounter copyFrom(MonitoredCounter templateCounter) {
		return build(templateCounter.getName(),
				templateCounter.getDescription(),
				templateCounter.getUnit());
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
