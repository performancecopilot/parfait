package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpMmvWriter.Store;

interface InstanceStoreFactory {

    Store<Instance> createNewInstanceStore(String name, InstanceDomain instanceDomain);

}
