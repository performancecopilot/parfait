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

package io.pcp.parfait.spring.timing;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import io.pcp.parfait.timing.EventTimer;
import io.pcp.parfait.timing.Timeable;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * A {@link BeanPostProcessor} responsible for adding all {@link io.pcp.parfait.timing.Timeable}s to the
 * specified {@link io.pcp.parfait.timing.EventTimer}
 */
public final class SpringEventTimerInjector implements BeanPostProcessor, BeanFactoryPostProcessor {
    private final EventTimer eventTimer;
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
