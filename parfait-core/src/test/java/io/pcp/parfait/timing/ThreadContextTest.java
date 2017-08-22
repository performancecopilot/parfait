/*
 * Copyright 2009-2017 Aconex
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.pcp.parfait.timing;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.MDC;

import junit.framework.TestCase;

public class ThreadContextTest extends TestCase {
    private ThreadContext context;
    
    public void setUp() {
        context = new ThreadContext();

        Hashtable hashtable = MDC.getContext();
        if (hashtable != null) {
            hashtable.clear();
        }
    }



    public void testGetOfUnusedKeyReturnsNull() {
        final String testKey = "handy";
        assertNull("get() of unused key should return null", context.get(testKey));
    }

    public void testGetRetrievesPutValue() {
        final String testKey = "brainy";
        final Object testValue = new Object();
        context.put(testKey, testValue);
        assertEquals(testValue, context.get(testKey));
    }

    public void testGetAfterRemoveReturnsNull() {
        final String testKey = "hefty";
        final Object testValue = new Object();
        context.put(testKey, testValue);
        context.remove(testKey);
        assertNull("remove() should result in null value for get()", context.get(testKey));
    }

    public void testCanAccessOtherThreadsContext() throws InterruptedException {
        final String testKey = "vanity";
        final Object testValue = new Object();
        final CountDownLatch finished = new CountDownLatch(1);
        final Thread otherThread = new Thread(new Runnable() {
            public void run() {
                context.put(testKey, testValue);
                finished.countDown();
            }
        });
        otherThread.start();
        finished.await();
        Map<String, Object> forThread = context.forThread(otherThread);
        assertEquals(1, forThread.size());
        assertEquals(testValue, forThread.get(testKey));
    }
    
    public void testGetsEmptyContextForUnknownThread() {
        assertEquals(0, context.forThread(new Thread()).size());
    }

    public void testClearRemovesValue() {
        final String testKey = "painter";
        context.put(testKey, 7);
        context.clear();
        assertNull("get() after clear should return null", context.get(testKey));
    }

    public void testClearRemovesMDCValue() {

        ThreadContext log4jThreadContext = ThreadContext.newMDCEnabledContext();

        Hashtable mdcContext = MDC.getContext();
        assertTrue(mdcContext == null || mdcContext.isEmpty());

        final String testKey = "painter";
        log4jThreadContext.put(testKey, 7);

        mdcContext = MDC.getContext();
        assertEquals(1, mdcContext.size());

        mdcContext.clear();
        assertEquals(0, mdcContext.size());

        log4jThreadContext.clear();
        assertNull("get() after clear should return null", log4jThreadContext.get(testKey));
    }
}
