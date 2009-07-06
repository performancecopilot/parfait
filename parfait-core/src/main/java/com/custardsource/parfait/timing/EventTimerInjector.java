package com.custardsource.parfait.timing;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * A {@link BeanPostProcessor} responsible for adding all {@link Timeable}s to the
 * specified {@link EventTimer}
 */
public final class EventTimerInjector implements BeanPostProcessor {

    private final EventTimer metricCollectorFactory;

    public EventTimerInjector(EventTimer metricCollectorFactory) {
        this.metricCollectorFactory = metricCollectorFactory;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        if (bean instanceof Timeable) {
            metricCollectorFactory.addController((Timeable) bean, beanName);
        }
        return bean;
    }

}
