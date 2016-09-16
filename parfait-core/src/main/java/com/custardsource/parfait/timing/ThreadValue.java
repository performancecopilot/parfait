package com.custardsource.parfait.timing;

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
