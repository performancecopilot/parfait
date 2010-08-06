package com.custardsource.parfait.spring.timing;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public final class BeanSpecification {
    private final String name;
    private final ConfigurableListableBeanFactory beanFactory;

    public BeanSpecification(String name, ConfigurableListableBeanFactory beanFactory) {
        this.name = name;
        this.beanFactory = beanFactory;
    }

    public String getName() {
        return name;
    }

    public ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }
}
