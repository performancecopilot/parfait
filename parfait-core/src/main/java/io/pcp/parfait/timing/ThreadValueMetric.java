package io.pcp.parfait.timing;

import javax.measure.Unit;

public class ThreadValueMetric extends AbstractThreadMetric {
    public ThreadValue<? extends Number> source;
    
    public ThreadValueMetric(String name, Unit<?> unit, String counterSuffix,
            String description, ThreadValue<? extends Number> source) {
        super(name, unit, counterSuffix, description);
        this.source = source;
    }

    @Override
    public long getValueForThread(Thread t) {
        Number n = source.getForThread(t);
        return n == null ? 0 : n.longValue();
    }
}
