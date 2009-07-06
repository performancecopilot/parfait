package com.custardsource.parfait.timing;


public interface ThreadMetric {
    long getCurrentValue();

    String getMetricName();

    String getUnit();

    String getCounterSuffix();

    String getDescription();
}