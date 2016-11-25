package io.pcp.parfait;

import static tec.uom.se.AbstractUnit.ONE;

import javax.measure.Unit;

public class DummyMonitorable implements Monitorable<String> {
    private final String name;

    public DummyMonitorable(String name) {
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
        return ONE;
    }

    @Override
    public void attachMonitor(Monitor m) {
    }

    @Override
    public void removeMonitor(Monitor m) {
    }

}
