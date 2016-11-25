package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.InstanceV2.InstanceStoreV2;
import io.pcp.parfait.dxm.PcpMmvWriter.Store;
import io.pcp.parfait.dxm.PcpString.PcpStringStore;

class InstanceStoreFactoryV2 implements InstanceStoreFactory {

    private IdentifierSourceSet identifierSourceSet;
    private PcpStringStore stringStore;

    InstanceStoreFactoryV2(IdentifierSourceSet identifierSourceSet, PcpStringStore stringStore) {
        this.identifierSourceSet = identifierSourceSet;
        this.stringStore = stringStore;
    }

    @Override
    public Store<Instance> createNewInstanceStore(String name, InstanceDomain instanceDomain) {
        return new InstanceStoreV2(identifierSourceSet, name, instanceDomain, stringStore);
    }

}
