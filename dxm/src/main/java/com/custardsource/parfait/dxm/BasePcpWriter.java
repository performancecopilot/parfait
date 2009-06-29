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

import com.custardsource.parfait.dxm.types.DefaultTypeHandlers;
import com.custardsource.parfait.dxm.types.TypeHandler;

public abstract class BasePcpWriter implements PcpWriter {
    private final File dataFile;
    private final Map<MetricName, PcpValueInfo> metricData = new LinkedHashMap<MetricName, PcpValueInfo>();
    private final Map<String, PcpMetricInfo> metricInfoByName = new LinkedHashMap<String, PcpMetricInfo>();
    private final Map<Integer, PcpMetricInfo> metricInfoById = new LinkedHashMap<Integer, PcpMetricInfo>();
    private final Map<Class<?>, TypeHandler<?>> typeHandlers = new HashMap<Class<?>, TypeHandler<?>>(
            DefaultTypeHandlers.getDefaultMappings());
    protected volatile boolean started = false;
    private ByteBuffer dataFileBuffer = null;

    protected BasePcpWriter(File dataFile) {
        this.dataFile = dataFile;
    }

    /*
     * (non-Javadoc)
     * @see com.custardsource.parfait.pcp.PcpWriter#addMetric(java.lang.String, java.lang.Object)
     */
    public void addMetric(MetricName name, Object initialValue) {
        TypeHandler<?> handler = typeHandlers.get(initialValue.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No default handler registered for type "
                    + initialValue.getClass());
        }
        addMetricInfo(name, initialValue, handler);

    }

    /*
     * (non-Javadoc)
     * @see com.custardsource.parfait.pcp.PcpWriter#addMetric(java.lang.String, T,
     * com.custardsource.parfait.pcp.types.TypeHandler)
     */
    public <T> void addMetric(MetricName name, T initialValue, TypeHandler<T> pcpType) {
        if (pcpType == null) {
            throw new IllegalArgumentException("PCP Type handler must not be null");
        }
        addMetricInfo(name, initialValue, pcpType);
    }

    /*
     * (non-Javadoc)
     * @see com.custardsource.parfait.pcp.PcpWriter#registerType(java.lang.Class,
     * com.custardsource.parfait.pcp.types.TypeHandler)
     */
    public <T> void registerType(Class<T> runtimeClass, TypeHandler<T> handler) {
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
    public void updateMetric(MetricName name, Object value) {
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

    @SuppressWarnings("unchecked")
    protected void updateValue(PcpValueInfo info, Object value) {
        TypeHandler rawHandler = info.getTypeHandler();
        dataFileBuffer.position(rawHandler.requiresLargeStorage() ? info.getLargeValue()
                .getOffset() : info.getOffset());
        rawHandler.putBytes(dataFileBuffer, value);
    }

    private synchronized void addMetricInfo(MetricName name, Object initialValue,
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
        
        PcpValueInfo info = new PcpValueInfo(name, metricInfo, instance, initialValue);
        metricData.put(name, info);
    }

    protected ByteBuffer initialiseBuffer(File file, int length) throws IOException {
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

    /*
     * (non-Javadoc)
     * @see com.custardsource.parfait.pcp.PcpWriter#start()
     */
    public void start() throws IOException {
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

    protected static class PcpMetricInfo {
        private final String metricName;
        private final int id;
        
        
        private InstanceDomain domain;
        private TypeHandler<?> typeHandler;
        private int offset;
        private PcpString shortHelpText;
        private PcpString longHelpText;
        

        public PcpMetricInfo(String metricName, int id) {
            this.metricName = metricName;
            this.id = id;
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
        
        public String getMetricName() {
            return metricName;
        }

        public TypeHandler<?> getTypeHandler() {
            return typeHandler;
        }
        
        private void setTypeHandler(TypeHandler<?> typeHandler) {
            if (this.typeHandler == null || this.typeHandler.equals(typeHandler)) {
                this.typeHandler = typeHandler;
            } else {
                throw new IllegalArgumentException(
                        "Two different type handlers cannot be registered for metric " + metricName);
            }
            
        }

        public InstanceDomain getInstanceDomain() {
            return domain;
        }

        private void setInstanceDomain(InstanceDomain domain) {
            if (this.domain == null || this.domain.equals(domain)) {
                this.domain = domain;
            } else {
                throw new IllegalArgumentException(
                        "Two different instance domains cannot be set for metric " + metricName);
            }
        }

        public PcpString getShortHelpText() {
            return shortHelpText;
        }
        
        public PcpString getLongHelpText() {
            return longHelpText;
        }

        public void setHelpText(PcpString shortHelpText, PcpString longHelpText) {
            this.shortHelpText = shortHelpText;
            this.longHelpText = longHelpText;
        }
}
    
    // TODO restore this to static - inject PCP String?
    protected class PcpValueInfo {

        public PcpValueInfo(MetricName metricName, PcpMetricInfo metricInfo, Instance instance, Object initialValue) {
            this.metricName = metricName;
            this.metricInfo = metricInfo;
            this.instance = instance;
            this.initialValue = initialValue;
            if (metricInfo.getTypeHandler().requiresLargeStorage()) {
                this.largeValue = createPcpString(initialValue.toString()); 
            } else {
                this.largeValue = null;
            }
        }

        private final MetricName metricName;
        private final Object initialValue;
        private final PcpMetricInfo metricInfo;
        private final Instance instance;
        private final PcpString largeValue;
        private int offset;

        public MetricName getMetricName() {
            return metricName;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public TypeHandler<?> getTypeHandler() {
            return metricInfo.typeHandler;
        }

        public Object getInitialValue() {
            return initialValue;
        }

        public int getInstanceOffset() {
            return instance == null ? 0 : instance.offset;
        }

        public int getDescriptorOffset() {
            return metricInfo.getOffset();
        }
        
        public PcpString getLargeValue() {
            return largeValue;
        }

    }

    private Map<String, InstanceDomain> instanceDomainsByName = new HashMap<String, InstanceDomain>();
    private Map<Integer, InstanceDomain> instanceDomainsById = new LinkedHashMap<Integer, InstanceDomain>();
    private Collection<PcpString> stringInfo = new ArrayList<PcpString>();

    // TODO don't synchronize - concurrentmap
    protected synchronized PcpMetricInfo getMetricInfo(String name) {
        PcpMetricInfo info = metricInfoByName.get(name);
        if (info == null) {
            info = new PcpMetricInfo(name, calculateId(name, metricInfoById.keySet()));
            metricInfoByName.put(name, info);
            metricInfoById.put(info.getId(), info);
        }
        return info;
    }

    // TODO don't synchronize - concurrentmap
    protected synchronized InstanceDomain getInstanceDomain(String name) {
        InstanceDomain domain = instanceDomainsByName.get(name);
        if (domain == null) {
            int id = calculateId(name, instanceDomainsById.keySet());
            domain = new InstanceDomain(name, id);
            instanceDomainsByName.put(name, domain);
            instanceDomainsById.put(id, domain);
        }
        return domain;
    }

    private static int calculateId(String name, Set<Integer> usedIds) {
        int value = name.hashCode();
        // Math.abs(MIN_VALUE) == MIN_VALUE, better deal with that just in case...
        if (value == Integer.MIN_VALUE) {
            value++;
        }
        value = Math.abs(value);
        while (usedIds.contains(value)) {
            if (value == Integer.MAX_VALUE) {
                value = 0;
            }
            value = Math.abs(value + 1);
        }
        return value;
    }

    protected static class InstanceDomain {
        private final String name;
        private final int id;
        private int offset;
        private Map<String, Instance> instancesByName = new HashMap<String, Instance>();
        private Map<Integer, Instance> instancesById = new LinkedHashMap<Integer, Instance>();
        private PcpString shortHelpText;
        private PcpString longHelpText;

        private InstanceDomain(String name, int id) {
            this.name = name;
            this.id = id;
        }

        // TODO don't synchronize - concurrentmap
        public synchronized Instance getInstance(String name) {
            Instance instance = instancesByName.get(name);
            if (instance == null) {
                int id = calculateId(name, instancesById.keySet());
                instance = new Instance(this, name, id);
                instancesByName.put(name, instance);
                instancesById.put(id, instance);
            }
            return instance;
        }

        @Override
        public String toString() {
            return name + " (" + id + ") " + instancesById.values().toString();
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
        
        public int getInstanceCount() {
            return instancesById.size();
        }

        public int getFirstInstanceOffset() {
            return instancesById.values().iterator().next().getOffset();
        }

        public Collection<Instance> getInstances() {
            return instancesById.values();
        }

        public void setHelpText(PcpString shortHelpText, PcpString longHelpText) {
            this.shortHelpText = shortHelpText;
            this.longHelpText = longHelpText;
            
        }

        public PcpString getShortHelpText() {
            return shortHelpText;
        }

        public PcpString getLongHelpText() {
            return longHelpText;
        }
        
    }

    protected static class Instance {
        private final String name;
        private final int id;
        private final InstanceDomain instanceDomain;
        private int offset;

        private Instance(InstanceDomain domain, String name, int id) {
            this.instanceDomain = domain;
            this.name = name;
            this.id = id;
        }

        @Override
        public String toString() {
            return name + " (" + id + ")";
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public InstanceDomain getInstanceDomain() {
            return instanceDomain;
        }
    }
    
    protected Collection<InstanceDomain> getInstanceDomains() {
        return instanceDomainsById.values();
    }

    protected Collection<Instance> getInstances() {
        Collection<Instance> instances = new ArrayList<Instance>();
        for (InstanceDomain domain : instanceDomainsById.values()) {
            instances.addAll(domain.instancesById.values());
        }
        return instances;
    }

    protected Collection<PcpValueInfo> getValueInfos() {
        return metricData.values();
    }
    
    protected Collection<PcpMetricInfo> getMetricInfos() {
        return metricInfoByName.values();
    }
    

    protected static class PcpString {
        final String initialValue;
        int offset;
        
        public PcpString(String value) {
            this.initialValue = value;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public String getInitialValue() {
            return initialValue;
        }
        
    }

    @Override
    public void setInstanceDomainHelpText(String instanceDomain, String shortHelpText, String longHelpText) {
        InstanceDomain domain = getInstanceDomain(instanceDomain);
        domain.setHelpText(createPcpString(shortHelpText), createPcpString(longHelpText));
    }

    @Override
    public void setMetricHelpText(String metricName, String shortHelpText, String longHelpText) {
        PcpMetricInfo info = getMetricInfo(metricName);
        info.setHelpText(createPcpString(shortHelpText), createPcpString(longHelpText));
    }
    
    private PcpString createPcpString(String text) {
        if (text == null) {
            return null;
        }
        PcpString string = new PcpString(text);
        stringInfo .add(string);
        return string;
    }

    protected Collection<PcpString> getStrings() {
        return stringInfo;
    }

    public static void main(String[] args) throws Exception {
        BasePcpWriter writer = new PcpMmvWriter(new File("/tmp/xmmv"));
        InstanceDomain id;
        id = writer.getInstanceDomain("aconex.smurfs");
        id = writer.getInstanceDomain("aconex.tasks");
        id = writer.getInstanceDomain("aconex.controllers");
        id = writer.getInstanceDomain("aconex.controllers");
        id.getInstance("TaskControl");
        id.getInstance("TaskControl");
        id.getInstance("SearchControlledDocControl");
        writer.getInstanceDomain("aconex.tasks").getInstance("");
        System.out.println(writer.instanceDomainsById.values());
    }
}
