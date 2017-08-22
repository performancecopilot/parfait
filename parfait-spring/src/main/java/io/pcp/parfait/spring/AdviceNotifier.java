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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.aspectj.AspectJPointcutAdvisor;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class AdviceNotifier implements BeanPostProcessor {
	private static final Logger LOG = LoggerFactory.getLogger(AdviceNotifier.class);
	private final AdvisedAware aspect;

	public AdviceNotifier(AdvisedAware aspect) {
		this.aspect = aspect;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		if (bean instanceof Advised) {
			Advised advised = (Advised) bean;

			for (Advisor advisor : advised.getAdvisors()) {
				if (advisor instanceof AspectJPointcutAdvisor) {
					String foundName = ((AbstractAspectJAdvice) ((AspectJPointcutAdvisor) advisor)
							.getAdvice()).getAspectName();
					if (aspect.getName().equals(foundName)) {
						LOG.info(String.format("Found bean '%s' advised by %s; injecting", beanName, aspect));
						try {
							aspect.addAdvised(advised.getTargetSource().getTarget(), beanName);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
		}
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}
}
