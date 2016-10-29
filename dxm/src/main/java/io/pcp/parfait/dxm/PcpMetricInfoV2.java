package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpMmvWriter.Store;

import java.nio.ByteBuffer;
import java.util.Set;

class PcpMetricInfoV2 extends PcpMetricInfo {
    private PcpString nameAsPcpString;

    PcpMetricInfoV2(String metricName, int id, PcpString nameAsPcpString) {
        super(metricName, id);
        this.nameAsPcpString = nameAsPcpString;
    }

    @Override
    public void writeToMmv(ByteBuffer byteBuffer) {
        throw new RuntimeException("Not implemented");
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
