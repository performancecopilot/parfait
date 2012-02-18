package com.custardsource.parfait.dxm;

import static com.google.common.collect.Maps.newConcurrentMap;

import javax.measure.unit.Unit;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.custardsource.parfait.dxm.semantics.Semantics;
import com.custardsource.parfait.dxm.types.DefaultTypeHandlers;
import com.custardsource.parfait.dxm.types.TypeHandler;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import net.jcip.annotations.GuardedBy;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public abstract class BasePcpWriter implements PcpWriter {
	// TODO only include in-use indoms/instances/metrics (/strings?) in the header
	private final Store<PcpMetricInfo> metricInfoStore;
    private final Store<InstanceDomain> instanceDomainStore;
	
    private final Map<MetricName, PcpValueInfo> metricData = Maps.newConcurrentMap();
    private final Map<Class<?>, TypeHandler<?>> typeHandlers = new ConcurrentHashMap<Class<?>, TypeHandler<?>>(
            DefaultTypeHandlers.getDefaultMappings());
    private volatile boolean started = false;
    @GuardedBy("itself")
    private volatile ByteBuffer dataFileBuffer = null;
    private final Object globalLock = new Object();

    private final Collection<PcpString> stringInfo = new CopyOnWriteArrayList<PcpString>();
    private final ByteBufferFactory byteBufferFactory;
    private final Map<PcpValueInfo,ByteBuffer> perMetricByteBuffers = newConcurrentMap();
    private volatile boolean usePerMetricLock = true;


    protected BasePcpWriter(File file, IdentifierSourceSet identifierSources) {
        this(new FileByteBufferFactory(file), identifierSources);
    }
    
    protected BasePcpWriter(ByteBufferFactory byteBufferFactory, IdentifierSourceSet identifierSources) {
        this.byteBufferFactory = byteBufferFactory;
        this.metricInfoStore = new MetricInfoStore(identifierSources);
        this.instanceDomainStore = new InstanceDomainStore(identifierSources);
    }

    public final void addMetric(MetricName name, Semantics semantics, Unit<?> unit, Object initialValue) {
        TypeHandler<?> handler = typeHandlers.get(initialValue.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No default handler registered for type "
                    + initialValue.getClass());
        }
        addMetricInfo(name, semantics, unit, initialValue, handler);

    }

    public final <T> void addMetric(MetricName name, Semantics semantics, Unit<?> unit, T initialValue, TypeHandler<T> pcpType) {
        if (pcpType == null) {
            throw new IllegalArgumentException("PCP Type handler must not be null");
        }
        addMetricInfo(name, semantics, unit, initialValue, pcpType);
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
            return;
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
        initialiseOffsets();

        dataFileBuffer = byteBufferFactory.build(getBufferLength());
        synchronized (globalLock) {
            populateDataBuffer(dataFileBuffer, metricData.values());
            preparePerMetricBufferSlices();
        }

        started = true;
    }

    private void preparePerMetricBufferSlices() {
        for (PcpValueInfo info : metricData.values()) {
            TypeHandler<?> rawHandler = info.getTypeHandler();
            int bufferPosition = rawHandler.requiresLargeStorage() ? info.getLargeValue()
                    .getOffset() : info.getOffset();
            // need to position the original buffer first, as the sliced buffer starts from there
            dataFileBuffer.position(bufferPosition);
            ByteBuffer metricByteBufferSlice = dataFileBuffer.slice();
            metricByteBufferSlice.limit(rawHandler.getDataLength());
            perMetricByteBuffers.put(info, metricByteBufferSlice);
            metricByteBufferSlice.order(dataFileBuffer.order());
        }
    }

    @Override
    public final void setInstanceDomainHelpText(String instanceDomain, String shortHelpText, String longHelpText) {
        InstanceDomain domain = getInstanceDomain(instanceDomain);
        domain.setHelpText(createPcpString(shortHelpText), createPcpString(longHelpText));
    }

    @Override
    public final void setMetricHelpText(String metricName, String shortHelpText, String longHelpText) {
        PcpMetricInfo info = getMetricInfo(metricName);
        if (!info.hasHelpText()) {
            info.setHelpText(createPcpString(shortHelpText), createPcpString(longHelpText));
        }
    }

    @SuppressWarnings("unchecked")
    protected final void updateValue(PcpValueInfo info, Object value) {
        @SuppressWarnings("rawtypes")
        TypeHandler rawHandler = info.getTypeHandler();

        if (usePerMetricLock) {
            writeValueWithLockPerMetric(info, value, rawHandler);
        } else {
            writeValueWithGlobalLock(info, value, rawHandler);
        }
    }

    private void writeValueWithLockPerMetric(PcpValueInfo info, Object value, TypeHandler rawHandler) {
        ByteBuffer perMetricByteBuffer = perMetricByteBuffers.get(info);
        synchronized (perMetricByteBuffer) {
            perMetricByteBuffer.position(0);
            rawHandler.putBytes(perMetricByteBuffer, value);
        }

    }

    private void writeValueWithGlobalLock(PcpValueInfo info, Object value, TypeHandler rawHandler) {
        synchronized (globalLock) {
            dataFileBuffer.position(rawHandler.requiresLargeStorage() ? info.getLargeValue()
                    .getOffset() : info.getOffset());
            rawHandler.putBytes(dataFileBuffer, value);
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

    protected abstract int getBufferLength();

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

    @Override
    public void reset() {
        metricData.clear();
    }
    
    private synchronized void addMetricInfo(MetricName name, Semantics semantics, Unit<?> unit,
            Object initialValue, TypeHandler<?> pcpType) {
        if (metricData.containsKey(name)) {
            throw new IllegalArgumentException("Metric " + name
                    + " has already been added to writer");
        }
        if (name.getMetric().getBytes(getCharset()).length > getMetricNameLimit()) {
            throw new IllegalArgumentException("Cannot add metric " + name
                    + "; name exceeds length limit");
        }
		if (name.hasInstance()
				&& name.getInstance().getBytes(getCharset()).length > getInstanceNameLimit()) {
			throw new IllegalArgumentException("Cannot add metric " + name
					+ "; instance name is too long");
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
        metricInfo.setUnit(unit);
        metricInfo.setSemantics(semantics);
        
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
    
    public void setPerMetricLock(boolean usePerMetricLock) {
        Preconditions.checkState(!started, "Cannot change use of perMetricLock when started");
        this.usePerMetricLock = usePerMetricLock;
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

		synchronized int size() {
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

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("byteBufferFactory", this.byteBufferFactory).toString();
    }

}
