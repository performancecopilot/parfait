package io.pcp.parfait;

import static tec.units.ri.AbstractUnit.ONE;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.measure.Unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
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

	private static final Timer POLLING_TIMER = new Timer(
			"PollingMonitoredValue-poller", true);
	private static Scheduler SHARED_TIMER_SCHEDULER = new TimerScheduler(
			POLLING_TIMER);

    private final Supplier<T> poller;

	/**
	 * All timer tasks that have been scheduled in PollingMonitoredValues;
	 * useful only for testing.
	 */
	private static final List<TimerTask> SCHEDULED_TASKS = new CopyOnWriteArrayList<TimerTask>();

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
        this(name, description, registry, updateInterval, poller, semantics, ONE);
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

	/**
	 * Creates a new {@link PollingMonitoredValue} with the specified polling
	 * interval.
	 * 
	 * @param updateInterval
	 *            how frequently the Poller should be checked for updates (may
	 *            not be less than {@link #MIN_UPDATE_INTERVAL}
	 */
	public PollingMonitoredValue(String name, String description,
			MonitorableRegistry registry, int updateInterval, Supplier<T> poller,
			ValueSemantics semantics, Unit<?> unit, Scheduler scheduler) {
		super(name, description, registry, poller.get(), unit, semantics);
		this.poller = poller;
		Preconditions.checkState(updateInterval >= MIN_UPDATE_INTERVAL,
				"updateInterval is too short.");
		TimerTask task = new PollerTask();
		SCHEDULED_TASKS.add(task);
		scheduler.schedule(new PollerTask(), updateInterval);
	}

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", getName()).add("description", getDescription()).add("poller", poller).toString();
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
    
	@VisibleForTesting
	static void runAllTasks() {
		for (TimerTask task : SCHEDULED_TASKS) {
			task.run();
		}
	}

	/**
	 * Convenient factory method to create pollers you don't care about keeping
	 * – that is, pollers which should be registered and start updating their
	 * value, but which you don't need to hold a reference to (because you will
	 * presumably just be modifying the polled source).
	 */
	public static <T> void poll(String name, String description,
			MonitorableRegistry registry, int updateInterval, Supplier<T> poller,
			ValueSemantics semantics, Unit<?> unit) {
		new PollingMonitoredValue<T>(name, description, registry,
				updateInterval, poller, semantics, unit);
	}
}
