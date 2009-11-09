package com.custardsource.parfait.dxm;

import java.util.Collection;
import java.util.Set;

import com.custardsource.parfait.dxm.BasePcpWriter.Store;

class InstanceDomain implements PcpId, PcpOffset {
    private final String name;
    private final int id;
    private int offset;
    private final Store<Instance> instanceStore;
    private PcpString shortHelpText;
    private PcpString longHelpText;

    InstanceDomain(String name, int id, IdentifierSourceSet instanceStores) {
        this.name = name;
        this.id = id;
        this.instanceStore = new InstanceStore(instanceStores);
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
        public InstanceStore(IdentifierSourceSet identifierSources) {
            super(identifierSources.instanceSource(InstanceDomain.this));
        }

        @Override
        protected Instance newInstance(String name, Set<Integer> usedIds) {
            return new Instance(InstanceDomain.this, name, identifierSource.calculateId(name,
                    usedIds));
        }

	}
}