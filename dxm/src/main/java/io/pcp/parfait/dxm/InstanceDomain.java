package io.pcp.parfait.dxm;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Set;

import io.pcp.parfait.dxm.PcpMmvWriter.Store;

class InstanceDomain implements PcpId, PcpOffset, MmvWritable {
    private final String name;
    private final int id;
    private int offset;
    private final Store<Instance> instanceStore;
    private PcpString shortHelpText;
    private PcpString longHelpText;

    InstanceDomain(String name, int id, IdentifierSourceSet instanceStores) {
        this.name = name;
        this.id = id;
        this.instanceStore = new InstanceStoreV1(instanceStores, name, this);
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
    
    private int getInstanceCount() {
        return instanceStore.size();
    }

    private int getFirstInstanceOffset() {
        return instanceStore.all().iterator().next().getOffset();
    }

    Collection<Instance> getInstances() {
        return instanceStore.all();
    }

    void setHelpText(PcpString shortHelpText, PcpString longHelpText) {
        this.shortHelpText = shortHelpText;
        this.longHelpText = longHelpText;
        
    }

    @Override
    public void writeToMmv(ByteBuffer byteBuffer) {
        byteBuffer.position(offset);
        writeInstanceDomainSection(byteBuffer);
        for (Instance instance : getInstances()) {
            instance.writeToMmv(byteBuffer);
        }
    }

    private void writeInstanceDomainSection(ByteBuffer dataFileBuffer) {
        dataFileBuffer.putInt(id);
        dataFileBuffer.putInt(getInstanceCount());
        dataFileBuffer.putLong(getFirstInstanceOffset());
        dataFileBuffer.putLong(getStringOffset(shortHelpText));
        dataFileBuffer.putLong(getStringOffset(longHelpText));
    }


    private long getStringOffset(PcpString text) {
        if (text == null) {
            return 0;
        }
        return text.getOffset();
    }

    private static class InstanceStoreV1 extends Store<Instance> {
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

    static final class InstanceDomainStore extends Store<InstanceDomain> {
        private final IdentifierSourceSet identifierSources;

        InstanceDomainStore(IdentifierSourceSet identifierSources) {
            super(identifierSources.instanceDomainSource());
            this.identifierSources = identifierSources;
        }

        @Override
        protected InstanceDomain newInstance(String name, Set<Integer> usedIds) {
            return new InstanceDomain(name, identifierSource.calculateId(name, usedIds), identifierSources);
        }

    }
}