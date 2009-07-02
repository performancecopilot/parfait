/**
 * 
 */
package com.custardsource.parfait.dxm;

import com.custardsource.parfait.dxm.BasePcpWriter.PcpId;
import com.custardsource.parfait.dxm.BasePcpWriter.PcpOffset;

final class Instance implements PcpId, PcpOffset {
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

    String getName() {
        return name;
    }

    InstanceDomain getInstanceDomain() {
        return instanceDomain;
    }
}