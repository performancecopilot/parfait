package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpMmvWriter.Store;

import java.nio.ByteBuffer;
import java.util.Set;

class InstanceV2 extends Instance {

    private PcpString nameAsString;

    InstanceV2(InstanceDomain domain, String name, int id, PcpString nameAsString) {
        super(domain, name, id);
        this.nameAsString = nameAsString;
    }

    @Override
    public void writeToMmv(ByteBuffer byteBuffer) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int byteSize() {
        throw new RuntimeException("Not implemented");
    }

    static class InstanceStoreV2 extends Store<Instance> {
        private InstanceDomain instanceDomain;
        private PcpMmvWriter pcpMmvWriter;

        InstanceStoreV2(IdentifierSourceSet identifierSources, String name, InstanceDomain instanceDomain, PcpMmvWriter pcpMmvWriter) {
            super(identifierSources.instanceSource(name));
            this.instanceDomain = instanceDomain;
            this.pcpMmvWriter = pcpMmvWriter;
        }

        @Override
        protected Instance newInstance(String name, Set<Integer> usedIds) {
            return new InstanceV2(instanceDomain, name, identifierSource.calculateId(name, usedIds), pcpMmvWriter.createPcpString(name));
        }
    }
}
