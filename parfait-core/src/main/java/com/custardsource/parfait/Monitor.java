package com.custardsource.parfait;

/**
 * A monitor is notified of any changes to the value of any {@link Monitorable} objects it is
 * attached to.
 */
public interface Monitor {

    void valueChanged(Monitorable<?> monitorable);

}
