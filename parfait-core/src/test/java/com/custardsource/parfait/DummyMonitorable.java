package com.custardsource.parfait;

import javax.measure.unit.Unit;

class DummyMonitorable implements Monitorable<String> {
    private final String name;

    DummyMonitorable(String name) {
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
