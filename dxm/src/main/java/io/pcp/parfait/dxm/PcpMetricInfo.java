package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.semantics.Semantics;
import io.pcp.parfait.dxm.types.TypeHandler;

import javax.measure.Unit;
import java.nio.ByteBuffer;

abstract class PcpMetricInfo implements PcpId, PcpOffset, MmvWritable {
    static final int DEFAULT_INSTANCE_DOMAIN_ID = -1;

    protected final String metricName;
    private final int id;

    protected InstanceDomain domain;
    protected TypeHandler<?> typeHandler;
    protected int offset;
    protected PcpString shortHelpText;
    protected PcpString longHelpText;
    private Unit<?> unit;
    private Semantics semantics;

    PcpMetricInfo(String metricName, int id) {
        this.metricName = metricName;
        this.id = id;
    }

    public final int getId() {
        return id;
    }

    @Override
    public final int getOffset() {
        return offset;
    }

    @Override
    public final void setOffset(int offset) {
        this.offset = offset;
    }

    final TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }

    final void setTypeHandler(TypeHandler<?> typeHandler) {
        if (this.typeHandler == null || this.typeHandler.equals(typeHandler)) {
            this.typeHandler = typeHandler;
        } else {
            throw new IllegalArgumentException(
                    "Two different type handlers cannot be registered for metric " + metricName);
        }

    }

    final void setInstanceDomain(InstanceDomain domain) {
        if (this.domain != null && !this.domain.equals(domain)) {
            throw new IllegalArgumentException(
                    "Two different instance domains cannot be set for metric " + metricName
                            + " (old=" + this.domain + ", new=" + domain + ")");
        }
        this.domain = domain;
    }

    final void setHelpText(PcpString shortHelpText, PcpString longHelpText) {
        this.shortHelpText = shortHelpText;
        this.longHelpText = longHelpText;
    }

    final public void setUnit(Unit<?> unit) {
        if (this.unit != null && !this.unit.equals(unit)) {
            throw new IllegalArgumentException(
                    "Two different units cannot be set for metric " + metricName
                    + " (old=" + this.unit + ", new=" + unit + ")");
        }
        this.unit = unit;
    }

    final public Unit<?> getUnit() {
        return unit;
    }

    final public void setSemantics(Semantics semantics) {
        if (this.semantics != null && semantics != this.semantics) {
            throw new IllegalArgumentException(
                    "Two different semantics cannot be set for metric " + metricName
                    + " (old=" + this.semantics + ", new=" + semantics + ")");
        }
        this.semantics = semantics;
    }

    final public Semantics getSemantics() {
        return semantics == null ? Semantics.NO_SEMANTICS : semantics;
    }

    final boolean hasHelpText() {
        return (shortHelpText != null || longHelpText != null);
    }

    @Override
    public abstract void writeToMmv(ByteBuffer byteBuffer);

    final protected long getStringOffset(PcpString text) {
        if (text == null) {
            return 0;
        }
        return text.getOffset();
    }

}
