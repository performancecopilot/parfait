package com.custardsource.parfait;

public abstract class MonitoredNumeric<T extends Number> extends MonitoredValue<T> {
    public MonitoredNumeric(String name, String description, MonitorableRegistry registry,
            T initialValue) {
        super(name, description, registry, initialValue);
    }
    
    public abstract void inc();
    public abstract void dec();
}
