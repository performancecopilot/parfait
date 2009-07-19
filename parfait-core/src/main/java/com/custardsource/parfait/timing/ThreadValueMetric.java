package com.custardsource.parfait.timing;

public class ThreadValueMetric extends AbstractThreadMetric {
    public ThreadValue<? extends Number> source;
    
    public ThreadValueMetric(String name, String unit, String counterSuffix,
            String description, ThreadValue<? extends Number> source) {
        super(name, unit, counterSuffix, description);
        this.source = source;
    }
    
    @Override
    public long getCurrentValue() {
        return source.get().longValue();
    }
}
