package io.pcp.parfait;

import static org.junit.Assert.assertEquals;
import static tec.units.ri.unit.Units.FARAD;

import org.junit.Test;

public class MonitoredConstantTest {
    @Test
    public void newMonitoredConstantHasCorrectSemantics() {
        assertEquals(ValueSemantics.CONSTANT, newConstant().getSemantics());
    }

    @Test
    public void newMonitoredConstantHasSuppliedValues() {
        assertEquals("AAA", newConstant().getName());
        assertEquals("BBB", newConstant().getDescription());
        assertEquals(7, newConstant().get().intValue());
        assertEquals(FARAD, newConstant().getUnit());
        assertEquals(Integer.class, newConstant().getType());
    }

    private MonitoredConstant<Integer> newConstant() {
        return new MonitoredConstant<Integer>("AAA", "BBB", new MonitorableRegistry(), 7, FARAD);
    }
}
