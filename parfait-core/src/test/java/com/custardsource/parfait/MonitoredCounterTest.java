package com.custardsource.parfait;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class MonitoredCounterTest {
    @Test
    public void initialValueIsZero() {
        assertEquals(0L, newCounter().get().intValue());
    }

    @Test
    public void incrementIncreasesValueByOne() {
        MonitoredCounter counter = newCounter();
        counter.inc();
        assertEquals(1L, counter.get().intValue());
    }

    @Test
    public void incrementIncreasesValueByProvidedAmmount() {
        MonitoredCounter counter = newCounter();
        counter.inc(77L);
        assertEquals(77L, counter.get().intValue());
    }

    private MonitoredCounter newCounter() {
        return new MonitoredCounter("A", "aaa", new MonitorableRegistry());
    }
}
