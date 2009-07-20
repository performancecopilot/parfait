package com.custardsource.parfait.timing;


public interface ThreadMetric {
    long getCurrentValue();
    
    long getValueForThread(Thread t);

    String getMetricName();

    String getUnit();

    String getCounterSuffix();

    String getDescription();
}