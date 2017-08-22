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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class CompositeCounterTest {
    @Test
    public void incrementActionsIncrementAllSubcounters() {
        SimpleCounter first = new SimpleCounter();
        SimpleCounter second = new SimpleCounter();

        CompositeCounter counter = new CompositeCounter(Arrays.asList(first, second));
        counter.inc();
        Assert.assertEquals(1, first.value);
        Assert.assertEquals(1, second.value);

        counter.inc(10);
        Assert.assertEquals(11, first.value);
        Assert.assertEquals(11, second.value);

    }

    private static final class SimpleCounter implements Counter {
        private int value;

        @Override
        public void inc() {
            inc(1);
        }

        @Override
        public void inc(long increment) {
            value += increment;
        }
    }
}
