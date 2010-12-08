package com.custardsource.parfait;

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

    @Test(expected = IllegalStateException.class)
    public void registerWhenFrozenThrowsException() {
        MonitorableRegistry registry = newRegistry();
        registry.freeze();
        registry.register(new DummyMonitorable("foo"));
    }

    @Test
    public void getMonitorablesOnNewRegistryReturnsEmptyCollection() {
        MonitorableRegistry registry = newRegistry();
        registry.freeze();
        assertTrue(registry.getMonitorables().isEmpty());
    }

    @Test
    public void getMonitorablesReturnsRegisteredMonitorable() {
        MonitorableRegistry registry = newRegistry();
        Monitorable<?> dummy = new DummyMonitorable("foo");
        registry.register(dummy);
        registry.freeze();
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
