package com.custardsource.parfait.dxm;

import javax.measure.unit.Unit;

import com.custardsource.parfait.dxm.semantics.Semantics;
import com.custardsource.parfait.dxm.types.TypeHandler;

final class PcpMetricInfo implements PcpId, PcpOffset {
    private static final String OLD = " (old=";
    private static final String NEW = ", new=";

    private final String metricName;
    private final int id;

    private InstanceDomain domain;
    private TypeHandler<?> typeHandler;
    private int offset;
    private PcpString shortHelpText;
    private PcpString longHelpText;
    private Unit<?> unit;
    private Semantics semantics;

    PcpMetricInfo(String metricName, int id) {
        this.metricName = metricName;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    String getMetricName() {
        return metricName;
    }

    TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }

    void setTypeHandler(TypeHandler<?> typeHandler) {
        if (this.typeHandler == null || this.typeHandler.equals(typeHandler)) {
            this.typeHandler = typeHandler;
        } else {
            throw new IllegalArgumentException(
                    "Two different type handlers cannot be registered for metric " + metricName);
        }

    }

    InstanceDomain getInstanceDomain() {
        return domain;
    }

    void setInstanceDomain(InstanceDomain domain) {
        if (this.domain != null && !this.domain.equals(domain)) {
            throw new IllegalArgumentException(
                    "Two different instance domains cannot be set for metric " + metricName
                            + OLD + this.domain + NEW + domain + ")");
        }
        this.domain = domain;
    }

    PcpString getShortHelpText() {
        return shortHelpText;
    }

    PcpString getLongHelpText() {
        return longHelpText;
    }

    void setHelpText(PcpString shortHelpText, PcpString longHelpText) {
        this.shortHelpText = shortHelpText;
        this.longHelpText = longHelpText;
    }

    public void setUnit(Unit<?> unit) {
        if (this.unit != null && !this.unit.equals(unit)) {
            throw new IllegalArgumentException(
                    "Two different units cannot be set for metric " + metricName
                    + OLD + this.unit + NEW + unit + ")");
        }
        this.unit = unit;
    }

    public Unit<?> getUnit() {
        return unit;
    }
    
    public void setSemantics(Semantics semantics) {
        if (this.semantics != null && semantics != this.semantics) {
            throw new IllegalArgumentException(
                    "Two different semantics cannot be set for metric " + metricName
                    + OLD + this.semantics + NEW + semantics + ")");
        }
        this.semantics = semantics;
    }

    public Semantics getSemantics() {
        return semantics == null ? Semantics.NO_SEMANTICS : semantics;
    }

    public boolean hasHelpText() {
        return (shortHelpText != null || longHelpText != null);
    }    
}