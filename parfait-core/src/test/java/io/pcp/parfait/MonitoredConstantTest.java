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

package io.pcp.parfait;

import static org.junit.Assert.assertEquals;
import static tec.uom.se.unit.Units.FARAD;

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
