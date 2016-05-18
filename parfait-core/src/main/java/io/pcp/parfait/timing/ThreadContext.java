package io.pcp.parfait.timing;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.jcip.annotations.ThreadSafe;

/**
 * <p>
 * Map-like functions to keep track of key/value pairs for application threads.
 * Keys are Strings, with values of any arbitrary object. Optionally keeps a log
 * framework's MDC weakly in sync with changes -- that is, there is no atomicity
 * guarantee so it's plausible that this class' context information and the
 * logger's will not be in a consistent state; however, log4j and logback's MDC
 * expose data <em>only</em> to the current thread via a thread-local so this is
 * unlikely to be a problem in practice.
 * </p>
 * <p>
 * Most methods operate on the context of the calling thread; only
 * {@link #forThread(Thread)} allows cross-thread information retrieval.
 * </p>
 * 
 * @author Cowan
 */
@ThreadSafe
public class ThreadContext {
    private static final CacheLoader<Thread, Map<String, Object>> NEW_CONTEXT_CREATOR = new CacheLoader<Thread, Map<String, Object>>() {
        @Override
        public Map<String, Object> load(Thread thread) throws Exception {
            return new ConcurrentHashMap<>();
        }
    };
    private final LoadingCache<Thread, Map<String, Object>> PER_THREAD_CONTEXTS = CacheBuilder.newBuilder().weakKeys().build(NEW_CONTEXT_CREATOR);

    private volatile MdcBridge mdcBridge = new NullMdcBridge();

    public ThreadContext() {
        this(new NullMdcBridge());
    }

    public ThreadContext(MdcBridge mdcBridge) {
        // TODO should that be a static variable..?
        this.mdcBridge=mdcBridge;
    }

    /**
     * Adds the given key/value pair to the current thread's context, and updates {@link MdcBridge} with
     * same.
     */
    public void put(String key, Object value) {
        PER_THREAD_CONTEXTS.getUnchecked(Thread.currentThread()).put(key, value);
        mdcBridge.put(key, value);
    }

    /**
     * Removes the given key from the current thread's context and {@link MdcBridge}.
     */
    public void remove(String key) {
        PER_THREAD_CONTEXTS.getUnchecked(Thread.currentThread()).remove(key);
        mdcBridge.remove(key);
    }

    /**
     * Retrieves the value corresponding to the supplied key for the current thread (null if no such
     * value exists)
     */
    public Object get(String key) {
        return PER_THREAD_CONTEXTS.getUnchecked(Thread.currentThread()).get(key);
    }

    /**
     * Clears all values for the current thread.
     */
    public void clear() {

        /**
         * Unfortunately log4j's MDC historically never had a mechanism to block remove keys,
         * so we're forced to do this one by one.
         */
        for (String key : allKeys()) {
           mdcBridge.remove(key);
        }

        PER_THREAD_CONTEXTS.getUnchecked(Thread.currentThread()).clear();
    }

    /**
     * Retrieves a copy of the thread context for the given thread
     */
    public Map<String, Object> forThread(Thread t) {
        return new HashMap<String, Object>(PER_THREAD_CONTEXTS.getUnchecked(t));
    }

    public Collection<String> allKeys() {
        Set<String> keys = new HashSet<String>();
        for (Map<String, Object> threadMdc : PER_THREAD_CONTEXTS.asMap().values()) {
            keys.addAll(threadMdc.keySet());
        }
        return keys;
    }

    public Object getForThread(Thread thread, String key) {
        return PER_THREAD_CONTEXTS.getUnchecked(thread).get(key);
    }

    /**
     * Factory method that creates a new ThreadContext initialized to also update Log4j's MDC.
     */
    public static ThreadContext newMDCEnabledContext() {
        return new ThreadContext(new Log4jMdcBridge());
    }

    /**
     * Factory method that creates a new ThreadContext initialised to also update SLF4J's MDC
     */
    public static ThreadContext newSLF4JEnabledContext() {
        return new ThreadContext(new Slf4jMDCBridge());
    }

    public interface MdcBridge {
    	void put(String key, Object object);

		void remove(String key);
    }
    
    public static class NullMdcBridge implements MdcBridge {
		@Override
		public void put(String key, Object object) {
			// no-op
		}

		@Override
		public void remove(String key) {
			// no-op
		}
    }
    
    public static class Log4jMdcBridge implements MdcBridge {
		@Override
		public void put(String key, Object object) {
			org.apache.log4j.MDC.put(key, object);
		}

		@Override
		public void remove(String key) {
			org.apache.log4j.MDC.remove(key);
		}
	}

    public static class Slf4jMDCBridge implements MdcBridge {
		@Override
		public void put(String key, Object object) {
			org.slf4j.MDC.put(key, String.valueOf(object));
		}

		@Override
		public void remove(String key) {
			org.slf4j.MDC.remove(key);
		}
	}

}
