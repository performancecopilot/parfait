/*
 * Copyright 2009-2017 Aconex
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.pcp.parfait.dxm.semantics;

import static tec.uom.se.AbstractUnit.ONE;

import tec.uom.se.unit.Units;

import systems.uom.quantity.Information;
import systems.uom.unicode.CLDR;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Time;
import javax.measure.Quantity;
import javax.measure.Unit;

interface PcpScale<T extends Quantity> extends UnitValued {
    public Unit<?> getUnit();

    static enum SpaceScale implements PcpScale<Information> {
        BYTE(0, CLDR.BYTE),
        KILOBYTE(1, CLDR.BYTE.multiply(1L << 10)),
        MEGABYTE(2, CLDR.BYTE.multiply(1L << 20)),
        GIGABYTE(3, CLDR.BYTE.multiply(1L << 30)),
        TERABYTE(4, CLDR.BYTE.multiply(1L << 40)),
        PETABYTE(5, CLDR.BYTE.multiply(1L << 50)),
        EXABYTE(6, CLDR.BYTE.multiply(1L << 60));

        private final int pmUnitsValue;
        private final Unit<Information> unit;

        private SpaceScale(int pmUnitsValue, Unit<Information> unit) {
            this.pmUnitsValue = pmUnitsValue;
            this.unit = unit;
        }

        @Override
        public Unit<Information> getUnit() {
            return unit;
        }

        @Override
        public int getPmUnitsValue() {
            return pmUnitsValue;
        }
    }

    static enum TimeScale implements PcpScale<Time> {
        NANOSECOND(0, Units.SECOND.divide(1000000000)),
        MICROSECOND(1, Units.SECOND.divide(1000000)),
        MILLISECOND(2, Units.SECOND.divide(1000)),
        SECOND(3, Units.SECOND),
        MINUTE(4, Units.MINUTE),
        HOUR(5, Units.HOUR);

        private final int pmUnitsValue;
        private final Unit<Time> unit;

        private TimeScale(int pmUnitsValue, Unit<Time> unit) {
            this.pmUnitsValue = pmUnitsValue;
            this.unit = unit;
        }

        @Override
        public Unit<Time> getUnit() {
            return unit;
        }

        @Override
        public int getPmUnitsValue() {
            return pmUnitsValue;
        }
    }

    static enum UnitScale implements PcpScale<Dimensionless> {
        UNIT(0, ONE),
        THOUSAND(3, ONE.multiply(1000)),
        MILLION(6, ONE.multiply(1000000)),
        BILLION(9, ONE.multiply(1000000000)),
        TRILLION(12, ONE.multiply(1000000000000L));

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
