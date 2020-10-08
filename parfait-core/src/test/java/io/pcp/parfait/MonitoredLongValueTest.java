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

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

public class MonitoredLongValueTest {
    @Test
    public void newMonitoredLongHasCorrectSemantics() {
        assertEquals(ValueSemantics.FREE_RUNNING, newValue().getSemantics());
    }

    @Test
    public void newMonitoredLongHasSuppliedValues() {
        assertEquals("AAA", newValue().getName());
        assertEquals("BBB", newValue().getDescription());
        assertEquals(23L, newValue().get().longValue());
        assertEquals(ONE, newValue().getUnit());
        assertEquals(AtomicLong.class, newValue().getType());
    }

    @Test
    public void incrementIncreasesValueByOne() {
        MonitoredLongValue value = newValue();
        value.inc();
        assertEquals(24L, value.get().longValue());
    }

    @Test
    public void decrementDecreasesValueByOne() {
        MonitoredLongValue value = newValue();
        value.dec();
        assertEquals(22, value.get().longValue());
    }

    @Test
    public void setReplacesValue() {
        MonitoredLongValue value = newValue();
        value.set(new AtomicLong(17));
        assertEquals(17L, value.get().longValue());
    }

    private MonitoredLongValue newValue() {
        return new MonitoredLongValue("AAA", "BBB", new MonitorableRegistry(), 23L);
    }
}
