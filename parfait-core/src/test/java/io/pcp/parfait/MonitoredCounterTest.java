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

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class MonitoredCounterTest {
    @Test
    public void monitoredCounterHasCorrectSemantics() {
        assertEquals(ValueSemantics.MONOTONICALLY_INCREASING, newCounter().getSemantics());
    }

    @Test
    public void initialValueIsZero() {
        assertEquals(0L, newCounter().get().longValue());
    }

    @Test
    public void incrementIncreasesValueByOne() {
        MonitoredCounter counter = newCounter();
        counter.inc();
        assertEquals(1L, counter.get().longValue());
    }

    @Test
    public void incrementIncreasesValueByProvidedAmount() {
        MonitoredCounter counter = newCounter();
        counter.inc(77L);
        assertEquals(77L, counter.get().longValue());
    }

    @Test
    public void canIncrementByNegativeNumber() {
        MonitoredCounter counter = newCounter();
        counter.inc(-1L);
        assertEquals(-1L, counter.get().longValue());
    }

    @Test
    public void canIncrementByZero() {
        MonitoredCounter counter = newCounter();
        counter.inc(0L);
        assertEquals(0L, counter.get().longValue());
    }

    @Test
    public void canSetToNewValue() {
        MonitoredCounter counter = newCounter();
        counter.set(1337L);
        assertEquals(1337L, counter.get().longValue());
    }

    private MonitoredCounter newCounter() {
        return new MonitoredCounter("A", "aaa", new MonitorableRegistry());
    }
}
