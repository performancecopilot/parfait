package com.custardsource.parfait;

import java.util.Calendar;

/**
 * An implementation of {@link Poller} to get the System time.
 *
 * @author mjerkovic
 */

public class SystemTimePoller implements Poller<Long> {

    public Long poll() {
        return Calendar.getInstance().getTimeInMillis();
    }

}
