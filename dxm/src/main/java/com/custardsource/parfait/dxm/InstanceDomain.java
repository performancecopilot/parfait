/**
 * 
 */
package com.custardsource.parfait.dxm;

import java.util.Collection;
import java.util.Set;

import com.custardsource.parfait.dxm.BasePcpWriter.PcpId;
import com.custardsource.parfait.dxm.BasePcpWriter.PcpOffset;
import com.custardsource.parfait.dxm.BasePcpWriter.Store;

class InstanceDomain implements PcpId, PcpOffset {
    private final String name;
    private final int id;
    private int offset;
    private final Store<Instance> instanceStore = new InstanceStore();
    private PcpString shortHelpText;
    private PcpString longHelpText;

    InstanceDomain(String name, int id) {
        this.name = name;
        this.id = id;
    }

    Instance getInstance(String name) {
    	return instanceStore.byName(name);
    }

    @Override
    public String toString() {
        return name + " (" + id + ") " + instanceStore.all().toString();
    }

    public int getId() {
        return id;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    int getInstanceCount() {
        return instanceStore.size();
    }

    int getFirstInstanceOffset() {
        return instanceStore.all().iterator().next().getOffset();
    }

    Collection<Instance> getInstances() {
        return instanceStore.all();
    }

    void setHelpText(PcpString shortHelpText, PcpString longHelpText) {
        this.shortHelpText = shortHelpText;
        this.longHelpText = longHelpText;
        
    }

    PcpString getShortHelpText() {
        return shortHelpText;
    }

    PcpString getLongHelpText() {
        return longHelpText;
    }
    
	private class InstanceStore extends Store<Instance> {
		@Override
		protected Instance newInstance(String name, Set<Integer> usedIds) {
			return new Instance(InstanceDomain.this, name, BasePcpWriter.calculateId(name, usedIds));
		}

	}
}