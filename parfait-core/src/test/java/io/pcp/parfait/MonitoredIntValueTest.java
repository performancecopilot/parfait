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
import static tech.units.indriya.AbstractUnit.ONE;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class MonitoredIntValueTest {
    @Test
    public void newMonitoredIntegerHasCorrectSemantics() {
        assertEquals(ValueSemantics.FREE_RUNNING, newValue().getSemantics());
    }

    @Test
    public void newMonitoredIntegerHasSuppliedValues() {
        assertEquals("AAA", newValue().getName());
        assertEquals("BBB", newValue().getDescription());
        assertEquals(23, newValue().get().intValue());
        assertEquals(ONE, newValue().getUnit());
        assertEquals(AtomicInteger.class, newValue().getType());
    }

    @Test
    public void incrementIncreasesValueByOne() {
        MonitoredIntValue value = newValue();
        value.inc();
        assertEquals(24, value.get().intValue());
    }

    @Test
    public void decrementDecreasesValueByOne() {
        MonitoredIntValue value = newValue();
        value.dec();
        assertEquals(22, value.get().intValue());
    }

    @Test
    public void setReplacesValue() {
        MonitoredIntValue value = newValue();
        value.set(new AtomicInteger(17));
        assertEquals(17, value.get().intValue());
    }

    private MonitoredIntValue newValue() {
        return new MonitoredIntValue("AAA", "BBB", new MonitorableRegistry(), 23);
    }
}
