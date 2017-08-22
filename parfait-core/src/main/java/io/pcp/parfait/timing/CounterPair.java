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

package io.pcp.parfait.timing;

import io.pcp.parfait.Counter;
import io.pcp.parfait.MonitoredCounter;

public class CounterPair implements Counter {
    private final MonitoredCounter masterCounter;
    private final ThreadCounter threadCounter;

    CounterPair(MonitoredCounter masterCounter, ThreadCounter threadCounter) {
        this.masterCounter = masterCounter;
        this.threadCounter = threadCounter;
    }

    @Override
    public void inc() {
        inc(1L);
    }

    @Override
    public void inc(long increment) {
        masterCounter.inc(increment);
        threadCounter.inc(increment);
    }

    public ThreadCounter getThreadCounter() {
        return threadCounter;
    }

    public MonitoredCounter getMasterCounter() {
        return masterCounter;
    }
}
