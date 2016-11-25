/**
 *
 */
package io.pcp.parfait.dxm;


import java.nio.ByteBuffer;

abstract class Instance implements PcpId, PcpOffset, MmvWritable {
    protected final String name;
    protected final int id;
    protected final InstanceDomain instanceDomain;
    protected int offset;

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
    public abstract void writeToMmv(ByteBuffer byteBuffer);

}