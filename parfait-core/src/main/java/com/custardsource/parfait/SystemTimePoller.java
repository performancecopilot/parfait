package com.custardsource.parfait;

import com.google.common.base.Supplier;

/**
 * An implementation of {@link Poller} to get the System time.
 */
public class SystemTimePoller implements Supplier<Long> {

    public Long get() {
        return System.currentTimeMillis();
    }

}
