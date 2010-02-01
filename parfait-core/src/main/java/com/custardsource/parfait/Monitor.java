package com.custardsource.parfait;

/**
 * A monitor is notified of any changes to the value of any {@link Monitorable}
 * objects it is attached to. This makes it effectively an 'output sink' for all
 * monitorable changes. When a Monitorable changes value, it will notify all
 * Monitors via {@link #valueChanged(Monitorable)}. Note that Monitors are under
 * no obligation to process this immediately — they may elect to queue the
 * notification for later update, swallow intermediate updates, etc., depending
 * on implementation.
 */
public interface Monitor {

    /**
     * Notifies the Monitor about a change in the underlying value of a
     * {@link Monitorable}. It is not guaranteed that the value obtained by
     * {@link Monitorable#get()} will return the value that triggered the
     * update, as the value may update in the meantime.
     * 
     * @param monitorable
     *            the Monitorable whose value has changed.
     */
    void valueChanged(Monitorable<?> monitorable);

}
