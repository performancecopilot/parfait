package com.custardsource.parfait.timing;

import java.util.concurrent.atomic.AtomicLong;

import javax.measure.unit.Unit;

public class DummyThreadMetric extends AbstractThreadMetric {
    public static final String METRIC_NAME = "dummy";
    public static final String METRIC_SUFFIX = "dummy.value";

    private AtomicLong value = new AtomicLong();

    public DummyThreadMetric(Unit<?> unit) {
        super(METRIC_NAME, unit, METRIC_SUFFIX, METRIC_NAME);
    }

    @Override
    public long getValueForThread(Thread t) {
        return value.get();
    }

    public void incrementValue(int amount) {
        value.addAndGet(amount);
    }
}
