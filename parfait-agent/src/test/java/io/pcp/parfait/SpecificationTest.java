/*
 * Copyright 2009-2017 Red Hat Inc.
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

package io.pcp.parfait;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static systems.uom.unicode.CLDR.BYTE;
import static tech.units.indriya.AbstractUnit.ONE;
import static javax.measure.MetricPrefix.MICRO;
import static javax.measure.MetricPrefix.MILLI;
import static javax.measure.MetricPrefix.NANO;
import static tech.units.indriya.unit.Units.SECOND;

import org.junit.Test;

public class SpecificationTest {

    @Test
    public void getUnits_shouldReturnMicrosecondUnits_whenASpecificationIsDefinedWithACustomMicrosecondsString() {
        final Specification specification = specificationWithUnit("microseconds");

        assertThat(specification.getUnits(), is(MICRO(SECOND)));
    }

    @Test
    public void getUnits_shouldReturnMicrosecondUnits_whenASpecificationIsDefinedWithACustomMicrosecondString() {
        final Specification specification = specificationWithUnit("microsecond");

        assertThat(specification.getUnits(), is(MICRO(SECOND)));
    }

    @Test
    public void getUnits_shouldReturnMicrosecondUnits_whenASpecificationIsDefinedWithTheUnitsOfMeasurementUsString() {
        final Specification specification = specificationWithUnit("µs");

        assertThat(specification.getUnits(), is(MICRO(SECOND)));
    }

    @Test
    public void getUnits_shouldReturnMillisecondUnits_whenASpecificationIsDefinedWithACustomMillisecondsString() {
        final Specification specification = specificationWithUnit("milliseconds");

        assertThat(specification.getUnits(), is(MILLI(SECOND)));
    }

    @Test
    public void getUnits_shouldReturnMillisecondUnits_whenASpecificationIsDefinedWithACustomMillisecondString() {
        final Specification specification = specificationWithUnit("millisecond");

        assertThat(specification.getUnits(), is(MILLI(SECOND)));
    }

    @Test
    public void getUnits_shouldReturnMillisecondUnits_whenASpecificationIsDefinedWithTheUnitsOfMeasurementMsString() {
        final Specification specification = specificationWithUnit("ms");

        assertThat(specification.getUnits(), is(MILLI(SECOND)));
    }

    @Test
    public void getUnits_shouldReturnNanosecondUnits_whenASpecificationIsDefinedWithACustomNanosecondsString() {
        final Specification specification = specificationWithUnit("nanoseconds");

        assertThat(specification.getUnits(), is(NANO(SECOND)));
    }

    @Test
    public void getUnits_shouldReturnNanosecondUnits_whenASpecificationIsDefinedWithACustomNanosecondString() {
        final Specification specification = specificationWithUnit("nanosecond");

        assertThat(specification.getUnits(), is(NANO(SECOND)));
    }

    @Test
    public void getUnits_shouldReturnNanosecondUnits_whenASpecificationIsDefinedWithACustomNsString() {
        final Specification specification = specificationWithUnit("ns");

        assertThat(specification.getUnits(), is(NANO(SECOND)));
    }

    @Test
    public void getUnits_shouldReturnByteUnits_whenASpecificationIsDefinedWithACustomBytesString() {
        final Specification specification = specificationWithUnit("bytes");

        assertThat(specification.getUnits(), is(BYTE));
    }

    @Test
    public void getUnits_shouldReturnByteUnits_whenASpecificationIsDefinedWithAUnitsOfMeasurementByteString() {
        final Specification specification = specificationWithUnit("byte");

        assertThat(specification.getUnits(), is(BYTE));
    }

    @Test
    public void getUnits_shouldReturnByteUnits_whenASpecificationIsDefinedWithAUnitsOfMeasurementBString() {
        final Specification specification = specificationWithUnit("B");

        assertThat(specification.getUnits(), is(BYTE));
    }

    @Test
    public void getUnits_shouldReturnKbyteUnits_whenASpecificationIsDefinedWithAUnitsOfMeasurementKbyteString() {
        final Specification specification = specificationWithUnit("Kbyte");

        assertThat(specification.getUnits(), is(BYTE.multiply(1L << 10)));
    }

    @Test
    public void getUnits_shouldReturnKbyteUnits_whenASpecificationIsDefinedWithAUnitsOfMeasurementKiBString() {
        final Specification specification = specificationWithUnit("KiB");

        assertThat(specification.getUnits(), is(BYTE.multiply(1L << 10)));
    }

    @Test
    public void getUnits_shouldReturnMbyteUnits_whenASpecificationIsDefinedWithAUnitsOfMeasurementMbyteString() {
        final Specification specification = specificationWithUnit("Mbyte");

        assertThat(specification.getUnits(), is(BYTE.multiply(1L << 20)));
    }

    @Test
    public void getUnits_shouldReturnMbyteUnits_whenASpecificationIsDefinedWithAUnitsOfMeasurementMiBString() {
        final Specification specification = specificationWithUnit("MiB");

        assertThat(specification.getUnits(), is(BYTE.multiply(1L << 20)));
    }

    @Test
    public void getUnits_shouldReturnGbyteUnits_whenASpecificationIsDefinedWithAUnitsOfMeasurementGbyteString() {
        final Specification specification = specificationWithUnit("Gbyte");

        assertThat(specification.getUnits(), is(BYTE.multiply(1L << 30)));
    }

    @Test
    public void getUnits_shouldReturnGbyteUnits_whenASpecificationIsDefinedWithAUnitsOfMeasurementGiBString() {
        final Specification specification = specificationWithUnit("GiB");

        assertThat(specification.getUnits(), is(BYTE.multiply(1L << 30)));
    }

    @Test
    public void getUnits_shouldReturnTbyteUnits_whenASpecificationIsDefinedWithAUnitsOfMeasurementTbyteString() {
        final Specification specification = specificationWithUnit("Tbyte");

        assertThat(specification.getUnits(), is(BYTE.multiply(1L << 40)));
    }

    @Test
    public void getUnits_shouldReturnTbyteUnits_whenASpecificationIsDefinedWithAUnitsOfMeasurementTiBString() {
        final Specification specification = specificationWithUnit("TiB");

        assertThat(specification.getUnits(), is(BYTE.multiply(1L << 40)));
    }

    @Test
    public void getUnits_shouldReturnEbyteUnits_whenASpecificationIsDefinedWithAUnitsOfMeasurementEbyteString() {
        final Specification specification = specificationWithUnit("Ebyte");

        assertThat(specification.getUnits(), is(BYTE.multiply(1L << 50)));
    }

    @Test
    public void getUnits_shouldReturnEbyteUnits_whenASpecificationIsDefinedWithAUnitsOfMeasurementKiBString() {
        final Specification specification = specificationWithUnit("EiB");

        assertThat(specification.getUnits(), is(BYTE.multiply(1L << 50)));
    }

    @Test
    public void getUnits_shouldReturnOneUnits_whenASpecificationIsDefinedWithAUnitsOfMeasurementNoneString() {
        final Specification specification = specificationWithUnit("none");

        assertThat(specification.getUnits(), is(ONE));
    }

    @Test
    public void getUnits_shouldReturnOneUnits_whenASpecificationIsDefinedWithAUnitsOfMeasurementOneString() {
        final Specification specification = specificationWithUnit("one");

        assertThat(specification.getUnits(), is(ONE));
    }

    @Test
    public void getUnits_shouldReturnOneUnits_whenASpecificationIsDefinedWithAUnitsOfMeasurementEmptyString() {
        final Specification specification = specificationWithUnit("");

        assertThat(specification.getUnits(), is(ONE));
    }

    @Test(expected = SpecificationException.class)
    public void new_throwsASpecificationExceptionIfTheUnitsCannotBeParsed() {
        specificationWithUnit("not-a-unit");
    }

    private Specification specificationWithUnit(String unit) {
        return new Specification("test", false, null, "", unit, null, "", "");
    }
}
