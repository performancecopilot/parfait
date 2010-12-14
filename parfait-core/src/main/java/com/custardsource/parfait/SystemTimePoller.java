package com.custardsource.parfait;

import java.util.Calendar;

import com.google.common.base.Supplier;

/**
 * An implementation of {@link Poller} to get the System time.
 */
public class SystemTimePoller implements Supplier<Long> {

    public Long get() {
        return Calendar.getInstance().getTimeInMillis();
    }

}
