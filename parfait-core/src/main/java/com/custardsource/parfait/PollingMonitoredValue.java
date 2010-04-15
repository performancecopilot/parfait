package com.custardsource.parfait;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import javax.measure.unit.Unit;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Monitors the value returned by calls at the provided interval to the provided
 * {@link Poller}.
 */
public class PollingMonitoredValue<T> extends SettableValue<T> {
    private static final Logger LOG = Logger.getLogger("parfait.polling");

    /**
     * The minimum time in ms that may be specified as an updateInterval.
     */
    private static final int MIN_UPDATE_INTERVAL = 250;

    private static final Timer POLLING_TIMER = new Timer("PollingMonitoredValue-poller", true);

    private final Poller<T> poller;

    /**
     * Creates a new {@link PollingMonitoredValue} with the specified polling
     * interval.
     * 
     * @param updateInterval
     *            how frequently the Poller should be checked for updates (may
     *            not be less than {@link #MIN_UPDATE_INTERVAL}
     */
    public PollingMonitoredValue(String name, String description,
            MonitorableRegistry registry, int updateInterval, Poller<T> poller, ValueSemantics semantics) {
        this(name, description, registry, updateInterval, poller, semantics, Unit.ONE);
    }

    /**
     * Creates a new {@link PollingMonitoredValue} with the specified polling
     * interval.
     *
     * @param updateInterval
     *            how frequently the Poller should be checked for updates (may
     *            not be less than {@link #MIN_UPDATE_INTERVAL}
     */
    public PollingMonitoredValue(String name, String description, MonitorableRegistry registry, int updateInterval,
            Poller<T> poller, ValueSemantics semantics, Unit<?> unit) {
        super(name, description, registry, poller.poll(), unit, semantics);
        this.poller = poller;
        Preconditions.checkState(updateInterval >= MIN_UPDATE_INTERVAL,
                "updateInterval is too short.");
        POLLING_TIMER.scheduleAtFixedRate(new PollerTask(), updateInterval, updateInterval);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    private class PollerTask extends TimerTask {
        @Override
        public void run() {
            try {
                set(poller.poll());
            } catch (Throwable t) {
                LOG.error("Error running poller " + this + "; will rerun next cycle", t);
            }
        }
    }
}