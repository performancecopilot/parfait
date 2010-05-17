package com.custardsource.parfait;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import javax.measure.unit.Unit;

import org.junit.Test;


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

    private MonitorableRegistry newRegistry() {
        return new MonitorableRegistry();
    }

    private static class DummyMonitorable implements Monitorable<String> {
        private final String name;

        private DummyMonitorable(String name) {
            this.name = name;
        }

        @Override
        public String get() {
            return "DummyValue";
        }

        @Override
        public String getDescription() {
            return "Blah";
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ValueSemantics getSemantics() {
            return ValueSemantics.CONSTANT;
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }

        @Override
        public Unit<?> getUnit() {
            return Unit.ONE;
        }

        @Override
        public void attachMonitor(Monitor m) {
        }

        @Override
        public void removeMonitor(Monitor m) {
        }

    }
}
