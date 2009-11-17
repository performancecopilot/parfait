package com.custardsource.parfait.dxm;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.measure.unit.Unit;

import com.custardsource.parfait.dxm.types.DefaultTypeHandlers;
import com.custardsource.parfait.dxm.types.TypeHandler;

public abstract class BasePcpWriter implements PcpWriter {
	// TODO concurrency safety audit
	// TODO only include in-use indoms/instances/metrics (/strings?) in the header
	// TODO config to presupply IDs and helptexts
	private final File dataFile;
	private final Store<PcpMetricInfo> metricInfoStore;
    private final Store<InstanceDomain> instanceDomainStore;
	
    private final Map<MetricName, PcpValueInfo> metricData = new LinkedHashMap<MetricName, PcpValueInfo>();
    private final Map<Class<?>, TypeHandler<?>> typeHandlers = new HashMap<Class<?>, TypeHandler<?>>(
            DefaultTypeHandlers.getDefaultMappings());
    private volatile boolean started = false;
    private ByteBuffer dataFileBuffer = null;
    private Collection<PcpString> stringInfo = new ArrayList<PcpString>();

    protected BasePcpWriter(File dataFile, IdentifierSourceSet identifierSources) {
        this.dataFile = dataFile;
        this.metricInfoStore = new MetricInfoStore(identifierSources);
        this.instanceDomainStore = new InstanceDomainStore(identifierSources);
    }

    public final void addMetric(MetricName name, Unit<?> unit, Object initialValue) {
        TypeHandler<?> handler = typeHandlers.get(initialValue.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No default handler registered for type "
                    + initialValue.getClass());
        }
        addMetricInfo(name, unit, initialValue, handler);

    }

    public final <T> void addMetric(MetricName name, Unit<?> unit, T initialValue, TypeHandler<T> pcpType) {
        if (pcpType == null) {
            throw new IllegalArgumentException("PCP Type handler must not be null");
        }
        addMetricInfo(name, unit, initialValue, pcpType);
    }

    /*
     * (non-Javadoc)
     * @see com.custardsource.parfait.pcp.PcpWriter#registerType(java.lang.Class,
     * com.custardsource.parfait.pcp.types.TypeHandler)
     */
    public final <T> void registerType(Class<T> runtimeClass, TypeHandler<T> handler) {
        if (started) {
            // Can't add any more metrics anyway; harmless
            return;
        }
        typeHandlers.put(runtimeClass, handler);
    }

    /*
     * (non-Javadoc)
     * @see com.custardsource.parfait.pcp.PcpWriter#updateMetric(java.lang.String, java.lang.Object)
     */
    public final void updateMetric(MetricName name, Object value) {
        if (!started) {
            throw new IllegalStateException("Cannot update metric unless writer is running");
        }
        PcpValueInfo info = metricData.get(name);
        if (info == null) {
            throw new IllegalArgumentException("Metric " + name
                    + " was not added before initialising the writer");
        }
        updateValue(info, value);
    }

    /*
     * (non-Javadoc)
     * @see com.custardsource.parfait.pcp.PcpWriter#start()
     */
    public final void start() throws IOException {
        if (started) {
            throw new IllegalStateException("Writer is already started");
        }
        if (metricData.isEmpty()) {
            throw new IllegalStateException("Cannot create an MMV file with no metrics");
        }
        initialiseOffsets();
        dataFileBuffer = initialiseBuffer(dataFile, getFileLength());
        populateDataBuffer(dataFileBuffer, metricData.values());

        started = true;
    }

    @Override
    public final void setInstanceDomainHelpText(String instanceDomain, String shortHelpText, String longHelpText) {
        InstanceDomain domain = getInstanceDomain(instanceDomain);
        domain.setHelpText(createPcpString(shortHelpText), createPcpString(longHelpText));
    }

    @Override
    public final void setMetricHelpText(String metricName, String shortHelpText, String longHelpText) {
        PcpMetricInfo info = getMetricInfo(metricName);
        info.setHelpText(createPcpString(shortHelpText), createPcpString(longHelpText));
    }

    @SuppressWarnings("unchecked")
    protected final void updateValue(PcpValueInfo info, Object value) {
        TypeHandler rawHandler = info.getTypeHandler();
        dataFileBuffer.position(rawHandler.requiresLargeStorage() ? info.getLargeValue()
                .getOffset() : info.getOffset());
        rawHandler.putBytes(dataFileBuffer, value);
    }

    private ByteBuffer initialiseBuffer(File file, int length) throws IOException {
        RandomAccessFile fos = null;
        try {
            fos = new RandomAccessFile(file, "rw");
            fos.setLength(0);
            fos.setLength(length);
            ByteBuffer tempDataFile = fos.getChannel().map(MapMode.READ_WRITE, 0, length);
            tempDataFile.order(ByteOrder.nativeOrder());
            fos.close();

            return tempDataFile;
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    protected abstract void initialiseOffsets();

    protected abstract void populateDataBuffer(ByteBuffer dataFileBuffer,
            Collection<PcpValueInfo> metricInfos) throws IOException;

    protected abstract int getMetricNameLimit();

    /**
     * @return the maximum length of an instance name supported by this agent. May be 0 to indicate
     *         that instances are not supported.
     */
    protected abstract int getInstanceNameLimit();

    protected abstract Charset getCharset();

    protected abstract int getFileLength();

    protected final PcpMetricInfo getMetricInfo(String name) {
    	return metricInfoStore.byName(name);
    }

    protected final Collection<PcpMetricInfo> getMetricInfos() {
        return metricInfoStore.all();
    }

    protected final InstanceDomain getInstanceDomain(String name) {
    	return instanceDomainStore.byName(name);
    }

    protected final Collection<InstanceDomain> getInstanceDomains() {
        return instanceDomainStore.all();
    }

    protected final Collection<Instance> getInstances() {
        Collection<Instance> instances = new ArrayList<Instance>();
        for (InstanceDomain domain : instanceDomainStore.all()) {
            instances.addAll(domain.getInstances());
        }
        return instances;
    }

    protected final Collection<PcpValueInfo> getValueInfos() {
        return metricData.values();
    }
    
    protected final Collection<PcpString> getStrings() {
        return stringInfo;
    }
    
    protected final File getDataFile() {
    	return dataFile;
    }


    private synchronized void addMetricInfo(MetricName name, Unit<?> unit, Object initialValue,
            TypeHandler<?> pcpType) {
        if (started) {
            throw new IllegalStateException("Cannot add metric " + name + " after starting");
        }
        if (metricData.containsKey(name)) {
            throw new IllegalArgumentException("Metric " + name
                    + " has already been added to writer");
        }
        if (name.getMetric().getBytes(getCharset()).length > getMetricNameLimit()) {
            throw new IllegalArgumentException("Cannot add metric " + name
                    + "; name exceeds length limit");
        }
        if (name.hasInstance()) {
            if (name.getInstance().getBytes(getCharset()).length > getInstanceNameLimit()) {
                throw new IllegalArgumentException("Cannot add metric " + name
                        + "; instance name is too long");
            }
        }
        PcpMetricInfo metricInfo = getMetricInfo(name.getMetric());
        InstanceDomain domain = null;
        Instance instance = null;
        
        if (name.hasInstance()) {
            domain = getInstanceDomain(name.getInstanceDomainTag());
            instance = domain.getInstance(name.getInstance());
            metricInfo.setInstanceDomain(domain);
        }
        metricInfo.setTypeHandler(pcpType);
        metricInfo.setUnit(unit == null ? Unit.ONE : unit);
        
        PcpValueInfo info = new PcpValueInfo(name, metricInfo, instance, initialValue, this);
        metricData.put(name, info);
    }

    PcpString createPcpString(String text) {
        if (text == null) {
            return null;
        }
        PcpString string = new PcpString(text);
        stringInfo .add(string);
        return string;
    }

	static abstract class Store<T extends PcpId> {
        private final Map<String, T> byName = new LinkedHashMap<String, T>();
        private final Map<Integer, T> byId = new LinkedHashMap<Integer, T>();
        protected final IdentifierSource identifierSource;
        
        public Store(IdentifierSource source) {
            this.identifierSource = source;
        }

        synchronized T byName(String name) {
	        T value = byName.get(name);
	        if (value == null) {
	            value = newInstance(name, byId.keySet());
	            byName.put(name, value);
	            byId.put(value.getId(), value);
	        }
	        return value;
		}
        
        synchronized Collection<T> all() {
        	return byName.values();
        }

		protected abstract T newInstance(String name, Set<Integer> usedIds);

		int size() {
			return byName.size();
		}
	}
	
    private static final class MetricInfoStore extends Store<PcpMetricInfo> {
        public MetricInfoStore(IdentifierSourceSet identifierSources) {
            super(identifierSources.metricSource());
        }
        
		@Override
		protected PcpMetricInfo newInstance(String name, Set<Integer> usedIds) {
			return new PcpMetricInfo(name, identifierSource.calculateId(name, usedIds));
		}
	}
    
    private static final class InstanceDomainStore extends Store<InstanceDomain> {
        private final IdentifierSourceSet identifierSources;
        
        public InstanceDomainStore(IdentifierSourceSet identifierSources) {
            super(identifierSources.instanceDomainSource());
            this.identifierSources = identifierSources;
        }

        @Override
		protected InstanceDomain newInstance(String name, Set<Integer> usedIds) {
            return new InstanceDomain(name, identifierSource.calculateId(name, usedIds), identifierSources);
		}
    	
    }
}
