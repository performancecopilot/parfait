package com.custardsource.parfait;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A collection of Monitorables to be monitored by a given output source (or
 * sources). Each {@link Monitorable} is associated with a particular
 * MonitorableRegistry, and informs the registry of its own details via a
 * callback to {@link #register(Monitorable)} when the Monitorable is created. A
 * {@link MonitorableRegistry} is then provided to a {@link AbstractMonitoringView},
 * which can optionally stop the addition of further Monitorables with
 * {@link #freeze()}, and then commence monitoring the monitorables in question.
 */
public class MonitorableRegistry {
    private static final ConcurrentMap<String, MonitorableRegistry> NAMED_INSTANCES = new ConcurrentHashMap<String, MonitorableRegistry>();

    /**
     * A single central registry which can be used my non-Registry-aware
     * Monitorables. This is very limiting in terms of system flexibility and an
     * explicit {@link MonitorableRegistry} should be used instead.
     */
    public static MonitorableRegistry DEFAULT_REGISTRY = new MonitorableRegistry();

    /**
     * This is a TreeMap so that the Monitorables are maintained in alphabetical
     * order for convenience.
     */
    private final Map<String, Monitorable<?>> monitorables = new TreeMap<String, Monitorable<?>>();

    // TODO this frozen thing has to go...
    private boolean stateFrozen = false;
    private final List<MonitorableRegistryListener> registryListeners = new CopyOnWriteArrayList<MonitorableRegistryListener>();

    /**
     * Informs this MonitorableRegistry of a new {@link Monitorable}; that
     * Monitorable will be added to the registry, assuming no Monitorable with
     * the same name has previously been registered.
     * 
     * @throws IllegalStateException
     *             if this registry has been frozen with {@link #freeze()}
     * @throws UnsupportedOperationException
     *             if the name of the provided monitorable has already been
     *             registered
     */
    public synchronized <T> void register(Monitorable<T> monitorable) {
        if (stateFrozen) {
            throw new IllegalStateException("Cannot register monitorable " + monitorable.getName()
                    + " after MonitorableRegistry has been frozen");
        }
        if (monitorables.containsKey(monitorable.getName())) {
            throw new UnsupportedOperationException(
                    "There is already an instance of the Monitorable [" + monitorable.getName()
                    + "] registered.");
        }
        monitorables.put(monitorable.getName(), monitorable);
        notifyListenersOfNewMonitorable(monitorable);
    }

    private void notifyListenersOfNewMonitorable(Monitorable<?> monitorable) {
        for (MonitorableRegistryListener listener : registryListeners) {
            listener.monitorableAdded(monitorable);
        }
    }

    /**
     * Locks this {@link MonitorableRegistry} so that no further metrics may be
     * added. To be used by {@link AbstractMonitoringView MonitoringViews} which do not
     * permit the addition of new metrics after startup.
     */
    public synchronized void freeze() {
        stateFrozen = true;
    }

    /**
     * @return a list of all Monitorables which are registered with this
     *         MonitorableRegistry.
     */
    public synchronized Collection<Monitorable<?>> getMonitorables() {
        return ImmutableList.copyOf(monitorables.values());
    }

    /*
     * Testing only -- should be eliminated once the default registry is gone
     */
    public static void clearDefaultRegistry() {
        DEFAULT_REGISTRY = new MonitorableRegistry();
    }

    /**
     * Retrieves or creates a centrally-accessible named instance, identified
     * uniquely by the provided String. This is a convenience method to bridge
     * between the old-style 'single registry' model (see
     * {@link #DEFAULT_REGISTRY}) and having to pass a MonitorableRegistry down
     * to the very depths of your class hierarchy. This is especially useful
     * when instrumenting third-party code which cannot easily get access to a
     * given MonitorableRegistry from a non-static context.
     * 
     * @param name
     * @return
     */
    public static MonitorableRegistry getNamedInstance(String name) {
        MonitorableRegistry instance = NAMED_INSTANCES.get(name);
        if (instance == null) {
            instance = new MonitorableRegistry();
            MonitorableRegistry existing = NAMED_INSTANCES.putIfAbsent(name, instance);
            if (existing != null) {
                return existing;
            }
        }
        return instance;
    }

    public void addRegistryListener(MonitorableRegistryListener monitorableRegistryListener) {
        this.registryListeners.add(monitorableRegistryListener);
    }
}