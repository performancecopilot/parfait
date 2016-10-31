package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpMmvWriter.Store;

import java.nio.ByteBuffer;
import java.util.Set;

import static io.pcp.parfait.dxm.PcpMmvWriter.PCP_CHARSET;

final class InstanceV1 extends Instance {

    /**
     * The maximum length of an instance name able to be exported to the MMV agent. Note that this
     * is relative to PCP_CHARSET (it's a measure of the maximum number of bytes, not the
     * Java String length)
     */
    static final int INSTANCE_NAME_LIMIT = 63;
    private static final int INSTANCE_LENGTH = 80;

    InstanceV1(InstanceDomain domain, String name, int id) {
        super(domain, name, id);
    }

    @Override
    public void writeToMmv(ByteBuffer byteBuffer) {
        byteBuffer.position(offset);
        byteBuffer.putLong(instanceDomain.getOffset());
        byteBuffer.putInt(0);
        byteBuffer.putInt(id);
        byteBuffer.put(name.getBytes(PCP_CHARSET));
    }

    @Override
    public int byteSize() {
        return INSTANCE_LENGTH;
    }

    static class InstanceStoreV1 extends Store<Instance> {
        private InstanceDomain instanceDomain;

        InstanceStoreV1(IdentifierSourceSet identifierSources, String name, InstanceDomain instanceDomain) {
            super(identifierSources.instanceSource(name));
            this.instanceDomain = instanceDomain;
        }

        @Override
        protected Instance newInstance(String name, Set<Integer> usedIds) {
            return new InstanceV1(instanceDomain, name, identifierSource.calculateId(name, usedIds));
        }

    }
}
