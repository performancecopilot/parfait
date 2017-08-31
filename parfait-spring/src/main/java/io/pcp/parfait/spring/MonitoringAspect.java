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

package io.pcp.parfait.spring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.BeanNameAware;

import io.pcp.parfait.timing.EventMetricCollector;
import io.pcp.parfait.timing.EventTimer;
import io.pcp.parfait.timing.Timeable;

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
