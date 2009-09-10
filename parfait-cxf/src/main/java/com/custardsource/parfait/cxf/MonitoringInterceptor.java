package com.custardsource.parfait.cxf;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.XMLMessage;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import com.custardsource.parfait.timing.EventMetricCollector;
import com.custardsource.parfait.timing.EventTimer;
import com.custardsource.parfait.timing.Timeable;

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
