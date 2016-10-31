package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpMmvWriter.Store;

import static io.pcp.parfait.dxm.InstanceV1.INSTANCE_NAME_LIMIT;
import static io.pcp.parfait.dxm.PcpMetricInfoV1.METRIC_NAME_LIMIT;
import static io.pcp.parfait.dxm.PcpString.STRING_BLOCK_LIMIT;

public enum MmvVersion {
    MMV_VERSION1(1, new MmvVersion1Factory()),
    MMV_VERSION2(2, new MmvVersion2Factory());

    private final int version;
    private final MmvVersionFactory mmvVersion1Factory;

    MmvVersion(int version, MmvVersionFactory mmvVersion1Factory) {
        this.version = version;
        this.mmvVersion1Factory = mmvVersion1Factory;
    }

    int getVersion() {
        return version;
    }

    Store<PcpMetricInfo> createMetricInfoStore(IdentifierSourceSet identifierSourceSet, PcpMmvWriter pcpMmvWriter) {
        return mmvVersion1Factory.createMetricInfoStore(identifierSourceSet, pcpMmvWriter);
    }

    Store<InstanceDomain> createInstanceDomainStore(IdentifierSourceSet identifierSourceSet, PcpMmvWriter pcpMmvWriter) {
        return mmvVersion1Factory.createInstanceDomainStore(identifierSourceSet, pcpMmvWriter);
    }

    MetricNameValidator createMetricNameValidator() {
        return mmvVersion1Factory.createMetricNameValidator();
    }

    interface MmvVersionFactory {
        Store<PcpMetricInfo> createMetricInfoStore(IdentifierSourceSet identifierSourceSet, PcpMmvWriter pcpMmvWriter);
        Store<InstanceDomain> createInstanceDomainStore(IdentifierSourceSet identifierSourceSet, PcpMmvWriter pcpMmvWriter);
        MetricNameValidator createMetricNameValidator();
    }

    private static class MmvVersion1Factory implements MmvVersionFactory {
        @Override
        public Store<PcpMetricInfo> createMetricInfoStore(IdentifierSourceSet identifierSourceSet, PcpMmvWriter unused) {
            return new PcpMetricInfoV1.MetricInfoStoreV1(identifierSourceSet);
        }

        @Override
        public Store<InstanceDomain> createInstanceDomainStore(IdentifierSourceSet identifierSourceSet, PcpMmvWriter unused) {
            return new InstanceDomain.InstanceDomainStore(
                    identifierSourceSet, new InstanceStoreFactoryV1(identifierSourceSet)
            );
        }

        @Override
        public MetricNameValidator createMetricNameValidator() {
            return new MetricNameValidator(METRIC_NAME_LIMIT, INSTANCE_NAME_LIMIT);
        }
    }

    private static class MmvVersion2Factory implements MmvVersionFactory {

        @Override
        public Store<PcpMetricInfo> createMetricInfoStore(IdentifierSourceSet identifierSourceSet, PcpMmvWriter pcpMmvWriter) {
            return new PcpMetricInfoV2.MetricInfoStoreV2(identifierSourceSet, pcpMmvWriter);
        }

        @Override
        public Store<InstanceDomain> createInstanceDomainStore(IdentifierSourceSet identifierSourceSet, PcpMmvWriter pcpMmvWriter) {
            return new InstanceDomain.InstanceDomainStore(identifierSourceSet,
                    new InstanceStoreFactoryV2(identifierSourceSet, pcpMmvWriter)
            );
        }

        @Override
        public MetricNameValidator createMetricNameValidator() {
            return new MetricNameValidator(STRING_BLOCK_LIMIT, STRING_BLOCK_LIMIT);
        }
    }
}
