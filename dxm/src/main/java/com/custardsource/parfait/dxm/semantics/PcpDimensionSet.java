/**
 * 
 */
package com.custardsource.parfait.dxm.semantics;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.custardsource.parfait.dxm.semantics.PcpScale.SpaceScale;
import com.custardsource.parfait.dxm.semantics.PcpScale.TimeScale;
import com.custardsource.parfait.dxm.semantics.PcpScale.UnitScale;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public enum PcpDimensionSet {
    SPACE(SpaceScale.class),
    TIME(TimeScale.class),
    EVENT(UnitScale.class),
    SPACE_PER_TIME(SpaceScale.class, TimeScale.class),
    SPACE_PER_EVENT(SpaceScale.class, UnitScale.class),
    TIME_PER_BYTE(TimeScale.class, SpaceScale.class),
    TIME_PER_EVENT(TimeScale.class, UnitScale.class),
    EVENTS_PER_SPACE(UnitScale.class, SpaceScale.class),
    EVENTS_PER_TIME(UnitScale.class, TimeScale.class);

    private final Collection<UnitMapping> unitMappings;

    <T extends PcpScale<?>> PcpDimensionSet(Class<T> unitDimension) {
        this.unitMappings = Collections2.transform(Arrays.asList(unitDimension
                .getEnumConstants()), new Function<PcpScale<?>, UnitMapping>() {
            @Override
            public UnitMapping apply(PcpScale<?> from) {
                return new UnitMapping(from.getUnit(), PcpDimensionSet.this, from, null);
            }
        });
    }

    <T extends PcpScale<?>, U extends PcpScale<?>> PcpDimensionSet(Class<T> unitDimension,
            Class<U> perDimension) {
        this.unitMappings = new HashSet<UnitMapping>();
        for (PcpScale<?> units : unitDimension.getEnumConstants()) {
            for (PcpScale<?> divisor : perDimension.getEnumConstants()) {
                unitMappings.add(new UnitMapping(units.getUnit().divide(divisor.getUnit()),
                        PcpDimensionSet.this, units, divisor));
            }
        }
    }

    public Collection<UnitMapping> getUnitMappings() {
        return unitMappings;
    }
}