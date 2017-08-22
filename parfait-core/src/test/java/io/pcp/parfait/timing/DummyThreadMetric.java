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

import java.util.concurrent.atomic.AtomicLong;

import javax.measure.Unit;

public class DummyThreadMetric extends AbstractThreadMetric {
    public static final String METRIC_NAME = "dummy";
    public static final String METRIC_SUFFIX = "dummy.value";

    private AtomicLong value = new AtomicLong();

    public DummyThreadMetric(Unit<?> unit) {
        super(METRIC_NAME, unit, METRIC_SUFFIX, METRIC_NAME);
    }

    @Override
    public long getValueForThread(Thread t) {
        return value.get();
    }

    public void incrementValue(int amount) {
        value.addAndGet(amount);
    }
}
