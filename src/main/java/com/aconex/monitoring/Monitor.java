package com.aconex.monitoring;

/**
 * A monitor is notified of any changes to the value of any {@link Monitorable} objects it is
 * attached to.
 *
 * @author ohutchison
 */
public interface Monitor<T> {

    void valueChanged(Monitorable<T> monitorable);

}
