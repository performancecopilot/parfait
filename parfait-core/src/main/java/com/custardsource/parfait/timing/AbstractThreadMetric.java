package com.custardsource.parfait.timing;

public abstract class AbstractThreadMetric implements ThreadMetric {
    private final String name;
    private final String unit;

    public AbstractThreadMetric(String name, String unit) {
        this.name = name;
        this.unit = unit;
    }

    public String getMetricName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }
}