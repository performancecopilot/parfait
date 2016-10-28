package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.semantics.Semantics;
import io.pcp.parfait.dxm.semantics.UnitMapping;
import io.pcp.parfait.dxm.types.TypeHandler;

import javax.measure.Unit;
import java.nio.ByteBuffer;

import static io.pcp.parfait.dxm.PcpMmvWriter.METRIC_NAME_LIMIT;
import static io.pcp.parfait.dxm.PcpMmvWriter.PCP_CHARSET;

abstract class PcpMetricInfo implements PcpId, PcpOffset, MmvWritable {
    private static final int DEFAULT_INSTANCE_DOMAIN_ID = -1;

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

    void setInstanceDomain(InstanceDomain domain) {
        if (this.domain != null && !this.domain.equals(domain)) {
            throw new IllegalArgumentException(
                    "Two different instance domains cannot be set for metric " + metricName
                            + " (old=" + this.domain + ", new=" + domain + ")");
        }
        this.domain = domain;
    }

    void setHelpText(PcpString shortHelpText, PcpString longHelpText) {
        this.shortHelpText = shortHelpText;
        this.longHelpText = longHelpText;
    }

    public void setUnit(Unit<?> unit) {
        if (this.unit != null && !this.unit.equals(unit)) {
            throw new IllegalArgumentException(
                    "Two different units cannot be set for metric " + metricName
                    + " (old=" + this.unit + ", new=" + unit + ")");
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
                    + " (old=" + this.semantics + ", new=" + semantics + ")");
        }
        this.semantics = semantics;
    }

    public Semantics getSemantics() {
        return semantics == null ? Semantics.NO_SEMANTICS : semantics;
    }

    boolean hasHelpText() {
        return (shortHelpText != null || longHelpText != null);
    }

    @Override
    public void writeToMmv(ByteBuffer byteBuffer) {
        byteBuffer.position(offset);

        int originalPosition = byteBuffer.position();

        byteBuffer.put(metricName.getBytes(PCP_CHARSET));
        byteBuffer.put((byte) 0);
        byteBuffer.position(originalPosition + METRIC_NAME_LIMIT + 1);
        byteBuffer.putInt(getId());
        byteBuffer.putInt(typeHandler.getMetricType().getIdentifier());
        byteBuffer.putInt(getSemantics().getPcpValue());
        byteBuffer.putInt(UnitMapping.getDimensions(getUnit(), metricName));
        if (domain != null) {
            byteBuffer.putInt(domain.getId());
        } else {
            byteBuffer.putInt(DEFAULT_INSTANCE_DOMAIN_ID);
        }
        // Just padding
        byteBuffer.putInt(0);
        byteBuffer.putLong(getStringOffset(shortHelpText));
        byteBuffer.putLong(getStringOffset(longHelpText));

    }

    private long getStringOffset(PcpString text) {
        if (text == null) {
            return 0;
        }
        return text.getOffset();
    }

}
