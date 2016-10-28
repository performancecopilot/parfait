/**
 *
 */
package io.pcp.parfait.dxm;


import java.nio.ByteBuffer;

import static io.pcp.parfait.dxm.PcpMmvWriter.PCP_CHARSET;

final class Instance implements PcpId, PcpOffset, MmvWritable {
    private final String name;
    private final int id;
    private final InstanceDomain instanceDomain;
    private int offset;

    Instance(InstanceDomain domain, String name, int id) {
        this.instanceDomain = domain;
        this.name = name;
        this.id = id;
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public int getId() {
        return id;
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