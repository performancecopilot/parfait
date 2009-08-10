package com.custardsource.parfait.timing;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

public class ThreadContext {
    private final Map<Thread, Map<String, String>> mdc = new MapMaker().weakKeys()
            .makeComputingMap(new Function<Thread, Map<String, String>>() {
                @Override
                public Map<String, String> apply(Thread from) {
                    // TODO not concurrent-safe
                    return new HashMap<String, String>();
                }
            });

    public void setThreadValue(String key, String value) {
        mdc.get(Thread.currentThread()).put(key, value);
    }

    public Object getThreadValue(Thread thread, String key) {
        return mdc.get(thread).get(key);
    }

    public Object getThreadValue(String key) {
        return getThreadValue(Thread.currentThread(), key);
    }

    public void clearThreadValue(String key) {
        mdc.get(Thread.currentThread()).remove(key);
    }
    
    public Map<String,Object> valuesForThread(Thread thread) {
        return new HashMap<String, Object>(mdc.get(thread));
    }

    public Collection<String> allKeys() {
        Set<String> keys = new HashSet<String>();
        for (Map<String, String> threadMdc : mdc.values()) {
            keys.addAll(threadMdc.keySet());
        }
        return keys;
    }
}
