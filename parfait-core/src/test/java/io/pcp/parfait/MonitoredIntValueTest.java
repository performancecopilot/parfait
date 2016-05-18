package io.pcp.parfait;

import static org.junit.Assert.assertEquals;
import static tec.units.ri.AbstractUnit.ONE;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class MonitoredIntValueTest {
    @Test
    public void newMonitoredIntegerHasCorrectSemantics() {
        assertEquals(ValueSemantics.FREE_RUNNING, newValue().getSemantics());
    }

    @Test
    public void newMonitoredIntegerHasSuppliedValues() {
        assertEquals("AAA", newValue().getName());
        assertEquals("BBB", newValue().getDescription());
        assertEquals(23, newValue().get().intValue());
        assertEquals(ONE, newValue().getUnit());
        assertEquals(AtomicInteger.class, newValue().getType());
    }

    @Test
    public void incrementIncreasesValueByOne() {
        MonitoredIntValue value = newValue();
        value.inc();
        assertEquals(24, value.get().intValue());
    }

    @Test
    public void decrementDecreasesValueByOne() {
        MonitoredIntValue value = newValue();
        value.dec();
        assertEquals(22, value.get().intValue());
    }

    @Test
    public void setReplacesValue() {
        MonitoredIntValue value = newValue();
        value.set(new AtomicInteger(17));
        assertEquals(17, value.get().intValue());
    }

    private MonitoredIntValue newValue() {
        return new MonitoredIntValue("AAA", "BBB", new MonitorableRegistry(), 23);
    }
}
