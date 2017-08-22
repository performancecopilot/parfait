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

package io.pcp.parfait.cxf;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.XMLMessage;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import io.pcp.parfait.timing.EventMetricCollector;
import io.pcp.parfait.timing.EventTimer;
import io.pcp.parfait.timing.Timeable;

public class MonitoringInterceptor extends AbstractPhaseInterceptor<XMLMessage> {
	private final EventTimer timer;

	public MonitoringInterceptor(EventTimer timer) {
		super(Phase.PRE_INVOKE);
		this.timer = timer;
	}

	@Override
	public void handleMessage(XMLMessage message) throws Fault {
		Exchange exchange = message.getExchange();
		// TODO break out into a strategy (not JAXRS-specific)
		OperationResourceInfo operationResourceInfo = exchange.get(OperationResourceInfo.class);
		if (operationResourceInfo == null) {
			message.getInterceptorChain().doIntercept(message);
			return;
		}

		// TODO annotate with a better name?
		String methodName = operationResourceInfo.getMethodToInvoke().getName();
		// TODO annotate with a better name?
		Object key = operationResourceInfo.getClassResourceInfo().getResourceProvider()
				.getInstance();
		if (!(key instanceof Timeable)) {
			message.getInterceptorChain().doIntercept(message);
			return;
		}

		EventMetricCollector collector = timer.getCollector();
		collector.startTiming((Timeable) key, methodName);
		try {
			message.getInterceptorChain().doIntercept(message);
		} finally {
			collector.stopTiming();
		}
	}
}
