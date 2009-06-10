package com.custardsource.parfait.timing;

public abstract class AbstractMetricSource implements ThreadMetric {
    private final String name;
    private final String unit;

    public AbstractMetricSource(String name, String unit) {
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