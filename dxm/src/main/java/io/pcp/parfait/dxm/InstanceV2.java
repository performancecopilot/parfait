package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpMmvWriter.Store;
import io.pcp.parfait.dxm.PcpString.PcpStringStore;

import java.nio.ByteBuffer;
import java.util.Set;

final class InstanceV2 extends Instance {

    private static final int INSTANCE_LENGTH = 24;
    private PcpString nameAsString;

    InstanceV2(InstanceDomain domain, String name, int id, PcpString nameAsString) {
        super(domain, name, id);
        this.nameAsString = nameAsString;
    }

    @Override
    public void writeToMmv(ByteBuffer byteBuffer) {
        byteBuffer.position(offset);
        byteBuffer.putLong(instanceDomain.getOffset());
        byteBuffer.putInt(0);
        byteBuffer.putInt(id);
        byteBuffer.putLong(getStringOffset(nameAsString));
    }

    private long getStringOffset(PcpString text) {
        if (text == null) {
            return 0;
        }
        return text.getOffset();
    }

    @Override
    public int byteSize() {
        return INSTANCE_LENGTH;
    }

    static class InstanceStoreV2 extends Store<Instance> {
        private InstanceDomain instanceDomain;
        private PcpStringStore stringStore;

        InstanceStoreV2(IdentifierSourceSet identifierSources, String name, InstanceDomain instanceDomain, PcpStringStore stringStore) {
            super(identifierSources.instanceSource(name));
            this.instanceDomain = instanceDomain;
            this.stringStore = stringStore;
        }

        @Override
        protected Instance newInstance(String name, Set<Integer> usedIds) {
            return new InstanceV2(instanceDomain, name, identifierSource.calculateId(name, usedIds), stringStore.createPcpString(name));
        }
    }
}
