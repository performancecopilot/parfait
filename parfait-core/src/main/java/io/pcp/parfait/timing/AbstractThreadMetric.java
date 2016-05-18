package io.pcp.parfait.timing;

import javax.measure.Unit;

public abstract class AbstractThreadMetric implements ThreadMetric {
    private final String name;
    private final Unit<?> unit;
    private final String counterSuffix;
    private final String description;

    public AbstractThreadMetric(String name, Unit<?> unit, String counterSuffix, String description) {
        this.name = name;
        this.unit = unit;
        this.counterSuffix = counterSuffix;
        this.description = description;
    }

    public String getMetricName() {
        return name;
    }

    public Unit<?> getUnit() {
        return unit;
    }

    public String getCounterSuffix() {
        return counterSuffix;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Metric[" + name + "]";
    }

    
}
