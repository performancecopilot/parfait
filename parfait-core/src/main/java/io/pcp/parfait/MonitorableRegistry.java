package io.pcp.parfait;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

/**
 * A collection of Monitorables to be monitored by a given output source (or
 * sources). Each {@link Monitorable} is associated with a particular
 * MonitorableRegistry, and informs the registry of its own details via a
 * callback to {@link #register(Monitorable)} when the Monitorable is created.
 * the MonitorableRegistryListener interface can be used for clients interested in state changes
 * to be made aware of when new Monitorables are added, such as objects that wish
 * to serialize state to external stores. (ie... A PCPmmvWriter.. say..)
 */
public class MonitorableRegistry {
    private static final ConcurrentMap<String, MonitorableRegistry> NAMED_INSTANCES = new ConcurrentHashMap<String, MonitorableRegistry>();

    /**
     * A single central registry which can be used by non-Registry-aware
     * Monitorables. This is very limiting in terms of system flexibility and an
     * explicit {@link MonitorableRegistry} should be used instead.
     */
    public static MonitorableRegistry DEFAULT_REGISTRY = new MonitorableRegistry();

    /**
     * This is a TreeMap so that the Monitorables are maintained in alphabetical
     * order for convenience.
     */
    private final Map<String, Monitorable<?>> monitorables = new TreeMap<String, Monitorable<?>>();

    private final List<MonitorableRegistryListener> registryListeners = new CopyOnWriteArrayList<MonitorableRegistryListener>();

    /**
     * Informs this MonitorableRegistry of a new {@link Monitorable}; that
     * Monitorable will be added to the registry, assuming no Monitorable with
     * the same name has previously been registered.
     * 
     * @throws UnsupportedOperationException
     *             if the name of the provided monitorable has already been
     *             registered
     */
    public synchronized <T> void register(Monitorable<T> monitorable) {
        if (monitorables.containsKey(monitorable.getName())) {
            throw new UnsupportedOperationException(
                    "There is already an instance of the Monitorable [" + monitorable.getName()
                    + "] registered.");
        }
        monitorables.put(monitorable.getName(), monitorable);
        notifyListenersOfNewMonitorable(monitorable);
    }

    /**
     * Registers the monitorable if it does not already exist, but otherwise returns an already registered
     * Monitorable with the same name, Semantics and UNnit definition.  This method is useful when objects
     * appear and disappear, and then return, and the lifecycle of the application requires an attempt to recreate
     * the Monitorable without knowing if it has already been created.
     *
     * If there exists a Monitorable with the same name, but with different Semantics or Unit then an IllegalArgumentException
     * is thrown.
     *
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T registerOrReuse(Monitorable<T> monitorable) {
        String name = monitorable.getName();
        if (monitorables.containsKey(name)) {
            Monitorable<?> existingMonitorableWithSameName = monitorables.get(name);
            if (monitorable.getSemantics().equals(existingMonitorableWithSameName.getSemantics()) && monitorable.getUnit().equals(existingMonitorableWithSameName.getUnit())) {
                return (T) existingMonitorableWithSameName;
            } else {
                throw new IllegalArgumentException(String.format("Cannot reuse the same name %s for a monitorable with different Semantics or Unit: requested=%s, existing=%s", name, monitorable, existingMonitorableWithSameName));
            }
        } else {
            monitorables.put(name, monitorable);
            notifyListenersOfNewMonitorable(monitorable);
            return (T) monitorable;
        }
    }

    private void notifyListenersOfNewMonitorable(Monitorable<?> monitorable) {
        for (MonitorableRegistryListener listener : registryListeners) {
            listener.monitorableAdded(monitorable);
        }
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


    public void removeRegistryListener(MonitorableRegistryListener listener) {
        this.registryListeners.remove(listener);
    }

    @VisibleForTesting
    boolean containsMetric(String name) {
        return monitorables.containsKey(name);
    }

    @VisibleForTesting
    Monitorable<?> getMetric(String name) {
        return monitorables.get(name);
    }

}
