package com.custardsource.parfait.timing;


public interface MetricSource {
    long getCurrentValue();

    String getMetricName();

    String getUnit();
}