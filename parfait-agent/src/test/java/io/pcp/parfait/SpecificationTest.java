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
import static org.junit.Assert.assertEquals;
import static systems.uom.unicode.CLDR.BYTE;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.SECOND;

import java.lang.reflect.Field;
import javax.measure.Unit;
import javax.measure.quantity.Time;

import org.hamcrest.Matchers;
import org.junit.Test;
import systems.uom.quantity.Information;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public class SpecificationTest {

    @Test
    public void getUnits_shouldReturnMillisecondUnitsWhenASpecificationIsDefinedWithAMillisecondString() {
        final Specification specification = new Specification("test", false, null, "", "milliseconds", null, "", "");

        assertThat(specification.getUnits(), is(MILLI(SECOND)));
    }

    @Test
    public void getUnits_shouldReturnByteUnitsWhenASpecificationIsDefinedWithABytesString() {
        final Specification specification = new Specification("test", false, null, "", "bytes", null, "", "");

        assertThat(specification.getUnits(), is(BYTE));
    }
}
