package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.InstanceV2.InstanceStoreV2;
import io.pcp.parfait.dxm.PcpMmvWriter.Store;

class InstanceStoreFactoryV2 implements InstanceStoreFactory {

    private IdentifierSourceSet identifierSourceSet;
    private PcpMmvWriter pcpMmvWriter;

    InstanceStoreFactoryV2(IdentifierSourceSet identifierSourceSet, PcpMmvWriter pcpMmvWriter) {
        this.identifierSourceSet = identifierSourceSet;
        this.pcpMmvWriter = pcpMmvWriter;
    }

    @Override
    public Store<Instance> createNewInstanceStore(String name, InstanceDomain instanceDomain) {
        return new InstanceStoreV2(identifierSourceSet, name, instanceDomain, pcpMmvWriter);
    }

}
