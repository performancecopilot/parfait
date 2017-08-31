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

package io.pcp.parfait;

import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;


public class MonitorableRegistryTest {

    @Test(expected = UnsupportedOperationException.class)
    public void registerWithDuplicateNameThrowsException() {
        MonitorableRegistry registry = newRegistry();
        Monitorable<?> dummy = new DummyMonitorable("foo");
        registry.register(dummy);
        Monitorable<?> dummy2 = new DummyMonitorable("foo");
        registry.register(dummy2);
    }

    @Test
    public void willReusePreviouslyRegisteredMetricInstead() {

        MonitorableRegistry registry = newRegistry();

        Monitorable<?> dummy = new DummyMonitorable("foo");
        registry.register(dummy);

        Monitorable<?> dummy2 = new DummyMonitorable("foo");
        Object reusedHopefully = registry.registerOrReuse(dummy2);
        assertSame("Should have returned the same object reference from the first registration, instead brought back " + reusedHopefully, dummy, reusedHopefully);
    }

    @Test
    public void getMonitorablesOnNewRegistryReturnsEmptyCollection() {
        MonitorableRegistry registry = newRegistry();
        assertTrue(registry.getMonitorables().isEmpty());
    }

    @Test
    public void getMonitorablesReturnsRegisteredMonitorable() {
        MonitorableRegistry registry = newRegistry();
        Monitorable<?> dummy = new DummyMonitorable("foo");
        registry.register(dummy);
        assertTrue(registry.getMonitorables().contains(dummy));
    }

    @Test
    public void getNamedInstanceReturnsExistingIfPresent() {
        MonitorableRegistry registry1 = MonitorableRegistry.getNamedInstance("xxx");
        MonitorableRegistry registry2 = MonitorableRegistry.getNamedInstance("xxx");
        assertSame(registry1, registry2);
    }

    @Test
    public void registryNotifiesOfChangeAfterNewRegistration(){

        final MonitorableRegistry monitorableRegistry = new MonitorableRegistry();

        final MonitorableRegistryListenerTester monitorableRegistryListener = new MonitorableRegistryListenerTester();
        monitorableRegistry.addRegistryListener(monitorableRegistryListener);

        final DummyMonitorable dummyMonitorable = new DummyMonitorable("foo");
        final DummyMonitorable dummyMonitorable2 = new DummyMonitorable("bar");

        monitorableRegistry.register(dummyMonitorable);

        assertTrue("Should have notified of new Monitorable added", monitorableRegistryListener.monitorablesAdded == 1);

        monitorableRegistry.register(dummyMonitorable2);

        assertTrue("Should have notified of second new Monitorable added", monitorableRegistryListener.monitorablesAdded == 2);
    }

    private MonitorableRegistry newRegistry() {
        return new MonitorableRegistry();
    }

    private static class MonitorableRegistryListenerTester implements MonitorableRegistryListener {

        private int monitorablesAdded = 0;

        @Override
        public void monitorableAdded(Monitorable<?> monitorable){
            monitorablesAdded++;
        }

    }
}
