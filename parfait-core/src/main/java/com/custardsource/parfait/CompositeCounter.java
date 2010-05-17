package com.custardsource.parfait;

import java.util.Collection;

import com.google.common.collect.Lists;

public class CompositeCounter implements Counter {
    private final Collection<Counter> counters;

    public CompositeCounter(Collection<? extends Counter> counters) {
        this.counters = Lists.newArrayList(counters);
    }

    @Override
    public void inc() {
        for (Counter counter : counters) {
            counter.inc();
        }
    }

    @Override
    public void inc(long increment) {
        for (Counter counter : counters) {
            counter.inc(increment);
        }
    }
}
