package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpMmvWriter.Store;
import io.pcp.parfait.dxm.semantics.UnitMapping;

import java.nio.ByteBuffer;
import java.util.Set;

class PcpMetricInfoV2 extends PcpMetricInfo {
    private static final int METRIC_LENGTH = 48;
    private PcpString nameAsPcpString;

    PcpMetricInfoV2(String metricName, int id, PcpString nameAsPcpString) {
        super(metricName, id);
        this.nameAsPcpString = nameAsPcpString;
    }

    @Override
    public void writeToMmv(ByteBuffer byteBuffer) {
        byteBuffer.position(offset);

        byteBuffer.putLong(getStringOffset(nameAsPcpString));
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

    @Override
    public int byteSize() {
        return METRIC_LENGTH;
    }

    static final class MetricInfoStoreV2 extends Store<PcpMetricInfo> {
        private PcpMmvWriter pcpMmvWriter;

        MetricInfoStoreV2(IdentifierSourceSet identifierSources, PcpMmvWriter pcpMmvWriter) {
            super(identifierSources.metricSource());
            this.pcpMmvWriter = pcpMmvWriter;
        }

        @Override
        protected PcpMetricInfo newInstance(String name, Set<Integer> usedIds) {
            return new PcpMetricInfoV2(name, identifierSource.calculateId(name, usedIds), pcpMmvWriter.createPcpString(name));
        }
    }
}
