package io.pcp.parfait.dxm.semantics;

import static org.junit.Assert.assertNotNull;
import static systems.uom.unicode.CLDR.BIT;
import static systems.uom.unicode.CLDR.BYTE;
import static tech.units.indriya.quantity.time.TimeQuantities.MILLISECOND;
import static tech.units.indriya.unit.Units.SECOND;

import javax.measure.MetricPrefix;

import org.junit.Test;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.unit.Units;

public class UnitMappingTest {

    @Test
    public void getUnitMapping_whenByteAndDerivativeUnitsGiven() {
        UnitMapping byteUnitMapping = UnitMapping.findUnitMapping(BYTE);
        assertNotNull(byteUnitMapping);

        UnitMapping bytePerSecondUnitMapping = UnitMapping.findUnitMapping(BYTE.divide(SECOND));
        assertNotNull(bytePerSecondUnitMapping);

        UnitMapping bytePerMilliSecondUnitMapping = UnitMapping.findUnitMapping(BYTE.divide(SECOND.divide(1000)));
        assertNotNull(bytePerMilliSecondUnitMapping);

        UnitMapping bitUnitMapping = UnitMapping.findUnitMapping(BIT);
        assertNotNull(bitUnitMapping);
    }

    @Test
    public void getUnitMapping_whenTimeAndDerivativeUnitsGiven() {
        UnitMapping milliSeconds = UnitMapping.findUnitMapping(SECOND.divide(1000));
        assertNotNull(milliSeconds);

        UnitMapping milliSeconds1 = UnitMapping.findUnitMapping(MetricPrefix.MILLI(SECOND));
        assertNotNull(milliSeconds1);

        UnitMapping milliSeconds2 = UnitMapping.findUnitMapping(MILLISECOND);
        assertNotNull(milliSeconds2);

        UnitMapping onePerMinute = UnitMapping.findUnitMapping(AbstractUnit.ONE.divide(Units.MINUTE));
        assertNotNull(onePerMinute);
    }
}
