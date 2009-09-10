package com.custardsource.parfait.spring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.BeanNameAware;

import com.custardsource.parfait.timing.EventMetricCollector;
import com.custardsource.parfait.timing.EventTimer;
import com.custardsource.parfait.timing.Timeable;

public class MonitoringAspect implements BeanNameAware, AdvisedAware {
	private final Map<Object, Timeable> map = new ConcurrentHashMap<Object, Timeable>();
	private final EventTimer timer;
	private String name;
	
	public MonitoringAspect(EventTimer timer) {
		this.timer = timer;
	}
	
	public Object profileMethod(ProceedingJoinPoint pjp) throws Throwable {
		Timeable timeable = map.get(pjp.getTarget());
		if (timeable == null) {
			return pjp.proceed();
		}
		EventMetricCollector collector = timer.getCollector();
		try {
			collector.startTiming(timeable, pjp.getSignature().getName());
			return pjp.proceed();
		} finally {
			collector.stopTiming();
		}
	}
	
	private static class DummyTimeable implements Timeable {
		@Override
		public void setEventTimer(EventTimer timer) {
		}
	}

	@Override
	public void setBeanName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addAdvised(Object advised, String name) {
		DummyTimeable timeable = new DummyTimeable();
		timer.registerTimeable(timeable, name);
		map.put(advised, timeable);
	}
}
