package io.pcp.parfait;

import static org.junit.Assert.assertEquals;

import static tec.uom.se.AbstractUnit.ONE;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

public class MonitoredLongValueTest {
    @Test
    public void newMonitoredLongHasCorrectSemantics() {
        assertEquals(ValueSemantics.FREE_RUNNING, newValue().getSemantics());
    }

    @Test
    public void newMonitoredLongHasSuppliedValues() {
        assertEquals("AAA", newValue().getName());
        assertEquals("BBB", newValue().getDescription());
        assertEquals(23L, newValue().get().longValue());
        assertEquals(ONE, newValue().getUnit());
        assertEquals(AtomicLong.class, newValue().getType());
    }

    @Test
    public void incrementIncreasesValueByOne() {
        MonitoredLongValue value = newValue();
        value.inc();
        assertEquals(24L, value.get().longValue());
    }

    @Test
    public void decrementDecreasesValueByOne() {
        MonitoredLongValue value = newValue();
        value.dec();
        assertEquals(22, value.get().longValue());
    }

    @Test
    public void setReplacesValue() {
        MonitoredLongValue value = newValue();
        value.set(new AtomicLong(17));
        assertEquals(17L, value.get().longValue());
    }

    private MonitoredLongValue newValue() {
        return new MonitoredLongValue("AAA", "BBB", new MonitorableRegistry(), 23L);
    }
}
