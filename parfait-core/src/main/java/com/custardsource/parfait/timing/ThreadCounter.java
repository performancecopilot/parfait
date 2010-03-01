package com.custardsource.parfait.timing;

import java.util.concurrent.atomic.AtomicLong;

import com.custardsource.parfait.Counter;

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
            map.get(Thread.currentThread()).addAndGet(increment);
        }
    }
}
