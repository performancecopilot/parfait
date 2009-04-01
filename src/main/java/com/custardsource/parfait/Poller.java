package com.custardsource.parfait;

/**
 * A Poller is called at regular intervals to retrieve a value from some source.
 *
 * @author ohutchison
 */
public interface Poller<T> {

    /**
     * Called to retrieve the polled value.
     * <p>
     * This method should not block and should return as fast as is possible.
     */
    T poll();

}
