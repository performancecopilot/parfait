package com.custardsource.parfait.dxm.semantics;


import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Dimensionless;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.custardsource.parfait.dxm.semantics.PcpScale.SpaceScale;
import com.custardsource.parfait.dxm.semantics.PcpScale.TimeScale;
import com.custardsource.parfait.dxm.semantics.PcpScale.UnitScale;

public final class UnitMapping {
    private static final Logger LOG = Logger.getLogger(UnitMapping.class);

    private final Unit<?> unit;
    private final PcpDimensionSet dimensionSet;
    private final PcpScale<?> unitDimension;
    private final PcpScale<?> perDimension;

    UnitMapping(Unit<?> unit, PcpDimensionSet dimensionSet, PcpScale<?> unitDimension,
            PcpScale<?> perDimension) {
        this.unit = unit;
        this.dimensionSet = dimensionSet;
        this.unitDimension = unitDimension;
        this.perDimension = perDimension;
    }

    @Override
    public String toString() {
        return "Mapping(unit=" + unit + " → " + dimensionSet + "=" + unitDimension
                + (perDimension == null ? "" : " per " + perDimension) + ")";
    }

    public static UnitMapping findUnitMapping(Unit<?> unit) {
        UnitMapping approximateMatch = null;

        for (PcpDimensionSet dimensionSet : PcpDimensionSet.values()) {
            for (UnitMapping mapping : dimensionSet.getUnitMappings()) {
                if (unit.equals(mapping.unit)) {
                    return mapping;
                }
                if (approximateMatch == null && areFunctionallyIdenticalUnits(unit, mapping.unit)) {
                    approximateMatch = mapping;
                }
            }
        }
        return approximateMatch;
    }

    private static boolean areFunctionallyIdenticalUnits(Unit<?> left, Unit<?> right) {
        if (!left.isCompatible(right)) {
            return false;
        }
        Unit<?> divided = left.divide(right);
        if (!divided.getDimension().equals(javax.measure.unit.Dimension.NONE)) {
            return false;
        }
        return divided.asType(Dimensionless.class).getConverterTo(Unit.ONE).equals(
                UnitConverter.IDENTITY);
    }

    public static int getDimensions(Unit<?> unit, String name) {
        if (unit == null) {
            return 0;
        }

        Dimension spaceDimension = Dimension.NONE;
        Dimension timeDimension = Dimension.NONE;
        Dimension unitDimension = Dimension.NONE;
        PcpScale<?> spaceScale = null;
        PcpScale<?> timeScale = null;
        PcpScale<?> unitScale = null;

        UnitMapping mapping = findUnitMapping(unit);

        if (mapping == null) {
            LOG.warn("No mapping found for unit " + unit + " of metric " + name
                    + "; treating as a unit quantity");
            unitDimension = Dimension.UNITS;
            unitScale = UnitScale.UNIT;
        } else {
            LOG.warn("Found mapping " + mapping + " for metric " + name);
            spaceDimension = mapping.getDimension(SpaceScale.class);
            timeDimension = mapping.getDimension(TimeScale.class);
            unitDimension = mapping.getDimension(UnitScale.class);
            spaceScale = mapping.getScale(SpaceScale.class);
            timeScale = mapping.getScale(TimeScale.class);
            unitScale = mapping.getScale(UnitScale.class);
        }
        return assembleDimensions(unitScale, timeScale, spaceScale, unitDimension, timeDimension,
                spaceDimension);
    }

    private PcpScale<?> getScale(Class<? extends PcpScale<?>> scaleClass) {
        if (perDimension != null && scaleClass.equals(perDimension.getClass())) {
            return perDimension;
        } else if (scaleClass.equals(unitDimension.getClass())) {
            return unitDimension;
        }
        return null;
    }

    private Dimension getDimension(Class<? extends PcpScale<?>> scaleClass) {
        if (perDimension != null && scaleClass.equals(perDimension.getClass())) {
            return Dimension.PER;
        } else if (scaleClass.equals(unitDimension.getClass())) {
            return Dimension.UNITS;
        }
        return Dimension.NONE;
    }

    private static int assembleDimensions(PcpScale<?> unitScale, PcpScale<?> timeScale,
            PcpScale<?> spaceScale, Dimension unitDimension, Dimension timeDimension,
            Dimension spaceDimension) {
        int result = 0;
        result |= to4Bits(unitScale) << 8;
        result |= to4Bits(timeScale) << 12;
        result |= to4Bits(spaceScale) << 16;
        result |= to4Bits(unitDimension) << 20;
        result |= to4Bits(timeDimension) << 24;
        result |= to4Bits(spaceDimension) << 28;
        return result;
    }

    private static int to4Bits(UnitValued valued) {
        return valued == null ? 0 : (valued.getPmUnitsValue() & 0xf);
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();

        System.out.println(UnitMapping.findUnitMapping(NonSI.BYTE));
        System.out.println(UnitMapping.findUnitMapping(NonSI.BYTE.divide(SI.SECOND)));
        System.out.println(UnitMapping.findUnitMapping(NonSI.BYTE.times(1024).divide(SI.SECOND)));
        System.out.println(UnitMapping.findUnitMapping(NonSI.BYTE.times(1024).divide(
                SI.SECOND.divide(1000))));
        System.out.println(UnitMapping.findUnitMapping(Unit.ONE.times(1000).divide(SI.SECOND)));
        System.out.println(UnitMapping.findUnitMapping(Unit.ONE.times(1000).divide(
                SI.SECOND.divide(1000))));
        System.out.println(UnitMapping.findUnitMapping(Unit.ONE.times(500).divide(
                SI.SECOND.divide(2))));
        System.out.println(UnitMapping.findUnitMapping(Unit.ONE.times(50).divide(SI.BIT)));
    }
}