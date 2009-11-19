package com.custardsource.parfait;

import javax.measure.unit.Unit;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.base.Preconditions;

abstract class SettableValue<T> extends AbstractMonitorable<T> {
    protected volatile T value;

    @SuppressWarnings("unchecked")
    protected SettableValue(String name, String description, MonitorableRegistry registry,
            T initialValue, Unit<?> unit, ValueSemantics semantics) {
        super(name, description, (Class<T>) initialValue.getClass(), unit, semantics);
        Preconditions.checkNotNull(initialValue, "Monitored value can not be null");
        this.value = initialValue;
        registerSelf(registry);
    }

    @Override
    public T get() {
        return value;
    }

    public void set(T newValue) {
        if (ObjectUtils.equals(this.value, newValue)) {
            return;
        }
        Preconditions.checkNotNull(newValue, "Monitored value can not be null");
        this.value = newValue;
        notifyMonitors();
    }

    @Override
    protected void logValue() {
        if (LOG.isTraceEnabled()) {
            LOG.trace(getName() + "=" + value);
        }
    }
}