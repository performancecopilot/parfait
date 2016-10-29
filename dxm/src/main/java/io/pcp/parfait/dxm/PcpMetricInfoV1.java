package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpMmvWriter.Store;

import java.util.Set;

final class PcpMetricInfoV1 extends PcpMetricInfo {
    private PcpMetricInfoV1(String metricName, int id) {
        super(metricName, id);
    }

    static final class MetricInfoStoreV1 extends Store<PcpMetricInfo> {
        MetricInfoStoreV1(IdentifierSourceSet identifierSources) {
            super(identifierSources.metricSource());
        }

        @Override
        protected PcpMetricInfo newInstance(String name, Set<Integer> usedIds) {
            return new PcpMetricInfoV1(name, identifierSource.calculateId(name, usedIds));
        }
    }

}
