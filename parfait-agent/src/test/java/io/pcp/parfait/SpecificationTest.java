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
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.SECOND;

import org.junit.Test;

public class SpecificationTest {

    @Test
    public void getUnits_shouldReturnMillisecondUnits_whenASpecificationIsDefinedWithACustomMillisecondsString() {
        final Specification specification = specificationWithUnit("milliseconds");

        assertThat(specification.getUnits(), is(MILLI(SECOND)));
    }

    @Test
    public void getUnits_shouldReturnMillisecondUnits_whenASpecificationIsDefinedWithTheUnitsOfMeasurementMsString() {
        final Specification specification = specificationWithUnit("ms");

        assertThat(specification.getUnits(), is(MILLI(SECOND)));
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

    @Test(expected = SpecificationException.class)
    public void new_throwsASpecificationExceptionIfTheUnitsCannotBeParsed() {
        specificationWithUnit("not-a-unit");
    }

    private Specification specificationWithUnit(String unit) {
        return new Specification("test", false, null, "", unit, null, "", "");
    }
}
