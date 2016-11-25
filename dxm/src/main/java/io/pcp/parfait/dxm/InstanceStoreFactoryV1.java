package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.InstanceV1.InstanceStoreV1;
import io.pcp.parfait.dxm.PcpMmvWriter.Store;

class InstanceStoreFactoryV1 implements InstanceStoreFactory {

    private IdentifierSourceSet instanceStores;

    InstanceStoreFactoryV1(IdentifierSourceSet instanceStores) {
        this.instanceStores = instanceStores;
    }

    @Override
    public Store<Instance> createNewInstanceStore(String name, InstanceDomain instanceDomain) {
        return new InstanceStoreV1(instanceStores, name, instanceDomain);
    }
}
