package com.custardsource.parfait;

/**
 * A monitor is notified of any changes to the value of any {@link Monitorable} objects it is
 * attached to.
 *
 * @author ohutchison
 */
public interface Monitor {

    void valueChanged(Monitorable<?> monitorable);

}
