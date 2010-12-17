package com.custardsource.parfait;

import java.util.Timer;
import java.util.TimerTask;

import javax.measure.unit.Unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

/**
 * Monitors the value returned by calls at the provided interval to the provided
 * {@link Poller}.
 */
public class PollingMonitoredValue<T> extends SettableValue<T> {
    private static final Logger LOG = LoggerFactory.getLogger("parfait.polling");

    /**
     * The minimum time in ms that may be specified as an updateInterval.
     */
    private static final int MIN_UPDATE_INTERVAL = 250;

    private static final Timer POLLING_TIMER = new Timer("PollingMonitoredValue-poller", true);

    private final Supplier<T> poller;

    /**
     * Creates a new {@link PollingMonitoredValue} with the specified polling
     * interval.
     *
     * @param updateInterval
     *            how frequently the Poller should be checked for updates (may
     *            not be less than {@link #MIN_UPDATE_INTERVAL}
     */
    public PollingMonitoredValue(String name, String description,
            MonitorableRegistry registry, int updateInterval, Supplier<T> poller, ValueSemantics semantics) {
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
            Supplier<T> poller, ValueSemantics semantics, Unit<?> unit) {
    	this(name, description, registry, updateInterval, poller, semantics, unit, SHARED_TIMER_SCHEDULER);
    }

	public PollingMonitoredValue(String name, String description,
			MonitorableRegistry registry, int updateInterval, Supplier<T> poller,
			ValueSemantics semantics, Unit<?> unit, Scheduler scheduler) {
		super(name, description, registry, poller.get(), unit, semantics);
		this.poller = poller;
		Preconditions.checkState(updateInterval >= MIN_UPDATE_INTERVAL,
				"updateInterval is too short.");
		scheduler.schedule(new PollerTask(), updateInterval);
	}

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", getName()).add("description", getDescription()).add("poller", poller).toString();
    }

    private class PollerTask extends TimerTask {
        @Override
        public void run() {
            try {
                set(poller.get());
            } catch (Throwable t) {
                LOG.error("Error running poller " + this + "; will rerun next cycle", t);
            }
        }
    }
    
    interface Scheduler {
    	public void schedule(TimerTask task, int rate);
    }
    
    private static Scheduler SHARED_TIMER_SCHEDULER = new Scheduler() {
		@Override
		public void schedule(TimerTask task, int rate) {
			POLLING_TIMER.scheduleAtFixedRate(task, rate, rate);
		}};
}