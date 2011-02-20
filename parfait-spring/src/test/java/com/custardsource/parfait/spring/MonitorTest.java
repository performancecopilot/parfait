package com.custardsource.parfait.spring;

import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.MonitoredCounter;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.sun.istack.internal.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

import java.util.Collection;
import java.util.Collections;


public class MonitorTest extends TestCase {
	public void testThing() {
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"test.xml"});
		DelayingBean test = ((DelayingBean) context.getBean("test"));
		DelayingBean other = ((DelayingBean) context.getBean("other"));

        MonitorableRegistry registry = (MonitorableRegistry) context.getBean("registry");

        assertNotNull("registry should not be null", registry);

        // TODO some Hamcrest matcher goodness on assertions that specific monitorable exists?

        assertTrue("should have located counter", counterExists(registry, "xx.other.count"));
        assertTrue("should have located counter", counterExists(registry, "xx.test.count"));

        MonitoredCounter otherCounter = (MonitoredCounter) findCounter(registry, "xx.other.count");

        assertNotNull("should have located the basic counter for 'other'", otherCounter);
        assertEquals(0L, otherCounter.get().longValue());


		test.doThing();
		other.doThing();

        assertEquals(1L, otherCounter.get().longValue());


	}

    private Monitorable<?> findCounter(MonitorableRegistry registry, final String name) {
        Collection<Monitorable<?>> monitorables = registry.getMonitorables();
        Monitorable<?> monitorable = Iterators.find(monitorables.iterator(), new Predicate<Monitorable<?>>() {
            @Override
            public boolean apply(@Nullable Monitorable<?> monitorable) {
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

    public void testAspectMethodCounters(){

        //ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"testAspectMethodCounters.xml"});

        //context.getBean("dao");

    }
}
