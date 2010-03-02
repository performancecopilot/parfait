package com.custardsource.parfait.timing;

import com.custardsource.parfait.Counter;
import com.custardsource.parfait.MonitoredCounter;

public class CounterPair implements Counter {
    private final MonitoredCounter masterCounter;
    private final ThreadCounter threadCounter;

    CounterPair(MonitoredCounter masterCounter, ThreadCounter threadCounter) {
        this.masterCounter = masterCounter;
        this.threadCounter = threadCounter;
    }

    @Override
    public void inc() {
        inc(1L);
    }

    @Override
    public void inc(long increment) {
        masterCounter.inc(increment);
        threadCounter.inc(increment);
    }

    public ThreadCounter getThreadCounter() {
        return threadCounter;
    }

    public MonitoredCounter getMasterCounter() {
        return masterCounter;
    }
}
