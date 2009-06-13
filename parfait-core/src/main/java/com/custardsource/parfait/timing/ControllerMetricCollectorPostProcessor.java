package com.custardsource.parfait.timing;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * A {@link BeanPostProcessor} responsible for adding all {@link MetricCollectorController}s to the
 * specified {@link ControllerMetricCollectorFactory}
 */
public final class ControllerMetricCollectorPostProcessor implements BeanPostProcessor {

    private final ControllerMetricCollectorFactory metricCollectorFactory;

    public ControllerMetricCollectorPostProcessor(
            ControllerMetricCollectorFactory metricCollectorFactory) {
        this.metricCollectorFactory = metricCollectorFactory;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        if (bean instanceof MetricCollectorController) {
            metricCollectorFactory.addController((MetricCollectorController) bean, beanName);
        }
        return bean;
    }

}
