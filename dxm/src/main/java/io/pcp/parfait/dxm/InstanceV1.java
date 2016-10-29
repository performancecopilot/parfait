package io.pcp.parfait.dxm;

import java.nio.ByteBuffer;

import static io.pcp.parfait.dxm.PcpMmvWriter.PCP_CHARSET;

final class InstanceV1 extends Instance {
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
}
