package com.custardsource.parfait.spring.timing;

import com.custardsource.parfait.timing.EventTimer;
import com.custardsource.parfait.timing.Timeable;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * A {@link BeanPostProcessor} responsible for adding all {@link com.custardsource.parfait.timing.Timeable}s to the
 * specified {@link com.custardsource.parfait.timing.EventTimer}
 */
public final class SpringEventTimerInjector implements BeanPostProcessor, BeanFactoryPostProcessor {
    private static final String GROUP_ATTRIBUTE = "parfaitTimingGroup";

    private final EventTimer eventTimer;
    private ApplicationContext applicationContext;
    private ConfigurableListableBeanFactory beanFactory;
    private final Predicate<? super BeanSpecification> beanPredicate;

    public SpringEventTimerInjector(EventTimer eventTimer) {
        this(eventTimer, Predicates.alwaysTrue());
    }

    public SpringEventTimerInjector(EventTimer eventTimer, Predicate<? super BeanSpecification> beanPredicate) {
        this.eventTimer = eventTimer;
        this.beanPredicate = beanPredicate;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        if (shouldMonitorBean(bean, beanName)) {
            eventTimer.registerTimeable((Timeable) bean, beanName);
        }
        return bean;
    }

    private boolean shouldMonitorBean(Object bean, String beanName) {
        if (!(bean instanceof Timeable)) {
            return false;
        }
        return beanPredicate.apply(new BeanSpecification(beanName, beanFactory));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

}
