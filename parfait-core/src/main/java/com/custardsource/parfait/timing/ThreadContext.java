package com.custardsource.parfait.timing;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.jcip.annotations.ThreadSafe;
import org.apache.log4j.MDC;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * <p>
 * Map-like functions to keep track of key/value pairs for application threads. Keys are Strings,
 * with values of any arbitrary object. Keeps the log4j {@link MDC} weakly in sync with changes --
 * that is, there is no atomicity guarantee so it's plausible that this class' context information
 * and log4js will not be in a consistent state; however, MDC exposes data <em>only</em> to the
 * current thread via a thread-local so this is unlikely to be a problem in practice.
 * </p>
 * <p>
 * Most methods operate on the context of the calling thread; only {@link #forThread(Thread)} allows
 * cross-thread information retrieval.
 * </p>
 *
 * @author Cowan
 */
@ThreadSafe
public class ThreadContext {
    private static final Function<Thread, Map<String, Object>> NEW_CONTEXT_CREATOR = new Function<Thread, Map<String, Object>>() {
        public Map<String, Object> apply(Thread thread) {
            return new ConcurrentHashMap<String, Object>();
        }
    };

    private final ConcurrentMap<Thread, Map<String, Object>> PER_THREAD_CONTEXTS = new MapMaker()
            .weakKeys().makeComputingMap(NEW_CONTEXT_CREATOR);

    /**
     * Adds the given key/value pair to the current thread's context, and updates {@link MDC} with
     * same.
     */
    public void put(String key, Object value) {
        PER_THREAD_CONTEXTS.get(Thread.currentThread()).put(key, value);
        MDC.put(key, value);
    }

    /**
     * Removes the given key from the current thread's context and {@link MDC}.
     */
    public void remove(String key) {
        PER_THREAD_CONTEXTS.get(Thread.currentThread()).remove(key);
        MDC.remove(key);
    }

    /**
     * Retrieves the value corresponding to the supplied key for the current thread (null if no such
     * value exists)
     */
    public Object get(String key) {
        return PER_THREAD_CONTEXTS.get(Thread.currentThread()).get(key);
    }

    /**
     * Clears all values for the current thread.
     */
    public void clear() {
        PER_THREAD_CONTEXTS.get(Thread.currentThread()).clear();
    }

    /**
     * Retrieves a copy of the thread context for the given thread
     */
    public Map<String, Object> forThread(Thread t) {
        return new HashMap<String, Object>(PER_THREAD_CONTEXTS.get(t));
    }

    public Collection<String> allKeys() {
        Set<String> keys = new HashSet<String>();
        for (Map<String, Object> threadMdc : PER_THREAD_CONTEXTS.values()) {
            keys.addAll(threadMdc.keySet());
        }
        return keys;
    }

    public Object getForThread(Thread thread, String key) {
        return PER_THREAD_CONTEXTS.get(thread).get(key);
    }

}
