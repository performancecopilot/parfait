package com.custardsource.parfait.timing;

public abstract class AbstractThreadMetric implements ThreadMetric {
    private final String name;
    private final String unit;
    private final String counterSuffix;
    private final String description;

    public AbstractThreadMetric(String name, String unit, String counterSuffix, String description) {
        this.name = name;
        this.unit = unit;
        this.counterSuffix = counterSuffix;
        this.description = description;
    }

    public String getMetricName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public String getCounterSuffix() {
        return counterSuffix;
    }

    public String getDescription() {
        return description;
    }

}