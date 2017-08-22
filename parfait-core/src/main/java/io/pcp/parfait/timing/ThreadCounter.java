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

import io.pcp.parfait.Counter;

public interface ThreadCounter extends ThreadValue<AtomicLong>, Counter {
    public static class ThreadLocalCounter extends ThreadValue.ThreadLocalMap<AtomicLong> implements ThreadCounter {

        public ThreadLocalCounter() {
            super(new ThreadLocal<AtomicLong>() {
                @Override
                protected AtomicLong initialValue() {
                    return new AtomicLong();
                }
            });
        }

        @Override
        public void inc() {
            inc(1L);
        }

        @Override
        public void inc(long increment) {
            threadLocal.get().addAndGet(increment);
        }
    }

    public static class ThreadMapCounter extends ThreadValue.WeakReferenceThreadMap<AtomicLong>
            implements ThreadCounter {

        public ThreadMapCounter() {
            super();
        }

        @Override
        protected AtomicLong initialValue() {
            return new AtomicLong();
        }

        @Override
        public void inc() {
            inc(1L);
        }

        @Override
        public void inc(long increment) {
            loadingCache.getUnchecked(Thread.currentThread()).addAndGet(increment);
        }
    }
}
