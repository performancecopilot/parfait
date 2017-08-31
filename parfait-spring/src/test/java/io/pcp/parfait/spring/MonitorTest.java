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

package io.pcp.parfait.spring;

import io.pcp.parfait.Monitorable;
import io.pcp.parfait.MonitorableRegistry;
import io.pcp.parfait.MonitoredCounter;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

import java.util.Collection;


public class MonitorTest extends TestCase {
    public void testThing() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"test.xml"});
        try {
            DelayingBean test = ((DelayingBean) context.getBean("test"));
            DelayingBean other = ((DelayingBean) context.getBean("other"));

            MonitorableRegistry registry = (MonitorableRegistry) context.getBean("registry");

            assertNotNull("registry should not be null", registry);

            assertTrue("should have located counter", counterExists(registry, "xx.other.count"));
            assertTrue("should have located counter", counterExists(registry, "xx.test.count"));

            MonitoredCounter otherCounter = (MonitoredCounter) findCounter(registry, "xx.other.count");
            MonitoredCounter testCounter = (MonitoredCounter) findCounter(registry, "xx.test.count");

            assertNotNull("should have located the basic counter for 'other'", otherCounter);
            assertEquals(0L, otherCounter.get().longValue());

            assertNotNull("should have located the basic counter for 'test'", testCounter);
            assertEquals(0L, testCounter.get().longValue());


            test.doThing();
            other.doThing();

            assertEquals(1L, otherCounter.get().longValue());
            assertEquals(1L, testCounter.get().longValue());
        } finally {
            context.close();
        }
    }

    // TODO some Hamcrest matcher goodness on assertions that specific monitorable exists?
    private Monitorable<?> findCounter(MonitorableRegistry registry, final String name) {
        Collection<Monitorable<?>> monitorables = registry.getMonitorables();
        Monitorable<?> monitorable = Iterators.find(monitorables.iterator(), new Predicate<Monitorable<?>>() {
            @Override
            public boolean apply(Monitorable<?> monitorable) {
                return monitorable.getName().equals(name);
            }
        });

        if (monitorable == null) {
            throw new IllegalArgumentException(name + " could not be located in the MonitorableRegistry");
        }
        return monitorable;
    }

    private boolean counterExists(MonitorableRegistry registry, String name) {
        for (Monitorable<?> monitorable: registry.getMonitorables()) {
            if (monitorable.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
