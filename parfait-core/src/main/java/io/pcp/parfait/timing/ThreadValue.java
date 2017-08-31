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

import java.util.Collections;
import java.util.Map;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public interface ThreadValue<T> {
    T get();

    boolean canRetrieveAcrossThreads();

    T getForThread(Thread thread);

    Map<Thread, T> asMap();

    public static class ThreadLocalMap<T> implements ThreadValue<T> {
        protected final ThreadLocal<? extends T> threadLocal;

        public ThreadLocalMap(ThreadLocal<? extends T> threadLocal) {
            this.threadLocal = threadLocal;
        }

        @Override
        public final boolean canRetrieveAcrossThreads() {
            return false;
        }

        @Override
        public final T get() {
            return threadLocal.get();
        }

        @Override
        public final T getForThread(Thread thread) {
            if (thread == Thread.currentThread()) {
                return threadLocal.get();
            }
            return null;
        }

        @Override
        public final Map<Thread, T> asMap() {
            return Collections.emptyMap();
        }
    }

    public static class WeakReferenceThreadMap<T> implements ThreadValue<T> {

        protected final LoadingCache<Thread, T> loadingCache = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<Thread, T>() {
            @Override
            public T load(Thread thread) throws Exception {
                return initialValue();
            }
        });

        protected T initialValue() {
            return null;
        }

        @Override
        public final boolean canRetrieveAcrossThreads() {
            return true;
        }

        @Override
        public final T get() {
            return loadingCache.getUnchecked(Thread.currentThread());
        }

        @Override
        public final T getForThread(Thread thread) {
            return loadingCache.getUnchecked(thread);
        }

        @Override
        public final Map<Thread, T> asMap() {
            return loadingCache.asMap();
        }
    }
}
