package com.custardsource.parfait.dxm.semantics;

import javax.measure.quantity.DataAmount;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

interface PcpScale<T extends Quantity> extends UnitValued {
    public Unit<T> getUnit();

    static enum SpaceScale implements PcpScale<DataAmount> {
        BYTE(0, NonSI.BYTE),
        KILOBYTE(1, NonSI.BYTE.times(1L << 10)),
        MEGABYTE(2, NonSI.BYTE.times(1L << 20)),
        GIGABYTE(3, NonSI.BYTE.times(1L << 30)),
        TERABYTE(4, NonSI.BYTE.times(1L << 40)),
        PETABYTE(5, NonSI.BYTE.times(1L << 50)),
        EXABYTE(6, NonSI.BYTE.times(1L << 60));

        private final int pmUnitsValue;
        private final Unit<DataAmount> unit;

        private SpaceScale(int pmUnitsValue, Unit<DataAmount> unit) {
            this.pmUnitsValue = pmUnitsValue;
            this.unit = unit;
        }

        @Override
        public Unit<DataAmount> getUnit() {
            return unit;
        }

        @Override
        public int getPmUnitsValue() {
            return pmUnitsValue;
        }
    }

    static enum TimeScale implements PcpScale<Duration> {
        NANOSECOND(0, SI.SECOND.divide(1000000000)),
        MICROSECOND(1, SI.SECOND.divide(1000000)),
        MILLISECOND(2, SI.SECOND.divide(1000)),
        SECOND(3, SI.SECOND),
        MINUTE(4, NonSI.MINUTE),
        HOUR(5, NonSI.HOUR);

        private final int pmUnitsValue;
        private final Unit<Duration> unit;

        private TimeScale(int pmUnitsValue, Unit<Duration> unit) {
            this.pmUnitsValue = pmUnitsValue;
            this.unit = unit;
        }

        @Override
        public Unit<Duration> getUnit() {
            return unit;
        }

        @Override
        public int getPmUnitsValue() {
            return pmUnitsValue;
        }
    }

    static enum UnitScale implements PcpScale<Dimensionless> {
        UNIT(0, Unit.ONE),
        THOUSAND(3, Unit.ONE.times(1000)),
        MILLION(6, Unit.ONE.times(1000000)),
        BILLION(9, Unit.ONE.times(1000000000)),
        TRILLION(12, Unit.ONE.times(1000000000000L));

        private final int pmUnitsValue;
        private final Unit<Dimensionless> unit;

        private UnitScale(int pmUnitsValue, Unit<Dimensionless> unit) {
            this.pmUnitsValue = pmUnitsValue;
            this.unit = unit;
        }

        @Override
        public Unit<Dimensionless> getUnit() {
            return unit;
        }

        @Override
        public int getPmUnitsValue() {
            return pmUnitsValue;
        }
    }
}