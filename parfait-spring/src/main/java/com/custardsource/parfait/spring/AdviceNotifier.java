package com.custardsource.parfait.spring;

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
						LOG.info("Found bean advised by " + aspect + "; injecting");
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
