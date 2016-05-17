package com.custardsource.parfait.timing;

import javax.measure.Unit;


public interface ThreadMetric {
    long getValueForThread(Thread t);

    String getMetricName();

    Unit<?> getUnit();

    String getCounterSuffix();

    String getDescription();
}
