package com.custardsource.parfait;

import java.util.List;

import javax.measure.unit.Unit;

import net.jcip.annotations.NotThreadSafe;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

@NotThreadSafe
public class PeriodicValueBuilder {
	private final List<PeriodicValue> values = Lists.newArrayList();
	private final Supplier<Long> timeSource;
	private final String baseName;
	private final String baseDescription;
	private final Unit<?> unit; 
	
	public PeriodicValueBuilder(String baseName, String baseDescription, Unit<?> unit) {
		this(baseName, baseDescription, unit, PeriodicValue.SYSTEM_TIME_SOURCE);
	}
	
	PeriodicValueBuilder(String baseName, String baseDescription, Unit<?> unit, Supplier<Long> timeSource) {
		this.timeSource = timeSource;
		this.baseName = baseName;
		this.baseDescription = baseDescription;
		this.unit = unit;
	}

	public void addPeriod(long resolution, long period, String name) {
		PeriodicValue value = new PeriodicValue(baseName + "." + name, baseDescription + " [" + name + "]", unit, resolution, period, timeSource);
	    values.add(value);
	}
	
	public CompositeCounter build() {
		return new CompositeCounter(values);
	}

}
