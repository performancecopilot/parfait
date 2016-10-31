package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpMmvWriter.Store;

import static io.pcp.parfait.dxm.InstanceV1.INSTANCE_NAME_LIMIT;
import static io.pcp.parfait.dxm.PcpMetricInfoV1.METRIC_NAME_LIMIT;

public enum MmvVersion {
    MMV_VERSION1(1, new MmvVersion1Factory());

    private final int version;
    private final MmvVersionFactory mmvVersion1Factory;

    MmvVersion(int version, MmvVersionFactory mmvVersion1Factory) {
        this.version = version;
        this.mmvVersion1Factory = mmvVersion1Factory;
    }

    int getVersion() {
        return version;
    }

    Store<PcpMetricInfo> createMetricInfoStore(IdentifierSourceSet identifierSourceSet) {
        return mmvVersion1Factory.createMetricInfoStore(identifierSourceSet);
    }

    Store<InstanceDomain> createInstanceDomainStore(IdentifierSourceSet identifierSourceSet) {
        return mmvVersion1Factory.createInstanceDomainStore(identifierSourceSet);
    }

    MetricNameValidator createMetricNameValidator() {
        return mmvVersion1Factory.createMetricNameValidator();
    }

    interface MmvVersionFactory {
        Store<PcpMetricInfo> createMetricInfoStore(IdentifierSourceSet identifierSourceSet);
        Store<InstanceDomain> createInstanceDomainStore(IdentifierSourceSet identifierSourceSet);
        MetricNameValidator createMetricNameValidator();
    }

    private static class MmvVersion1Factory implements MmvVersionFactory {
        @Override
        public Store<PcpMetricInfo> createMetricInfoStore(IdentifierSourceSet identifierSourceSet) {
            return new PcpMetricInfoV1.MetricInfoStoreV1(identifierSourceSet);
        }

        @Override
        public Store<InstanceDomain> createInstanceDomainStore(IdentifierSourceSet identifierSourceSet) {
            return new InstanceDomain.InstanceDomainStore(
                    identifierSourceSet, new InstanceStoreFactoryV1(identifierSourceSet)
            );
        }

        @Override
        public MetricNameValidator createMetricNameValidator() {
            return new MetricNameValidator(METRIC_NAME_LIMIT, INSTANCE_NAME_LIMIT);
        }
    }
}
