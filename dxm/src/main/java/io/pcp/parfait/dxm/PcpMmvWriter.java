/*
 * Copyright 2009-2017 Aconex
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.pcp.parfait.dxm;

import static com.google.common.collect.Maps.newConcurrentMap;
import static io.pcp.parfait.dxm.MmvVersion.MMV_VERSION1;
import static io.pcp.parfait.dxm.PcpString.STRING_BLOCK_LENGTH;
import static io.pcp.parfait.dxm.PcpString.STRING_BLOCK_LIMIT;
import static systems.uom.unicode.CLDR.BYTE;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.HERTZ;
import static tech.units.indriya.unit.Units.HOUR;
import static tech.units.indriya.unit.Units.SECOND;
import static tech.units.indriya.AbstractUnit.ONE;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.measure.Unit;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Monitor;
import io.pcp.parfait.dxm.PcpString.PcpStringStore;
import io.pcp.parfait.dxm.semantics.Semantics;
import io.pcp.parfait.dxm.types.AbstractTypeHandler;
import io.pcp.parfait.dxm.types.DefaultTypeHandlers;
import io.pcp.parfait.dxm.types.MmvMetricType;
import io.pcp.parfait.dxm.types.TypeHandler;
import com.google.common.base.Preconditions;
import net.jcip.annotations.GuardedBy;

/**
 * <p>
 * Creates and updates a memory-mapped file suitable for reading by the PCP MMV PMDA. The method
 * used to 'render' the Java values into the file (converting them to a corresponding PCP Type and
 * byte array) are determined by {@link TypeHandler TypeHandlers}; some default TypeHandlers are
 * supplied (see {@link DefaultTypeHandlers#getDefaultMappings()}) but additional ones can be
 * supplied for a particular Java class ({@link #registerType(Class, TypeHandler)}, or on a
 * metric-by-metric basis.
 * </p>
 * Standard lifecycle for this class is:
 * <ul>
 * <li>create the PcpMmvFile
 * <li>{@link #addMetric(MetricName, Semantics, Unit, Object)} for every metric which will be monitored by the file
 * <li>{@link #start()} to write out the file ready for the MMV agent to read
 * <li>{@link #updateMetric(MetricName, Object)} metrics as new values come to hand
 * </ul>
 * <p>
 * Adding metrics after the file is started will result in an {@link IllegalStateException}, as will
 * updating metric values <em>before</em> it's started.
 * </p>
 * <p>
 * This class currently has a few important limitations:
 * </p>
 * <ul>
 * <li>Process ID is obtained in a HotSpot-JVM specific way (may work on other JVMs, not known)</li>
 * <li>Receiving agent must be using MMV agent from pcp-2.8.10 or later (v1 MMV on-disk format)</li>
 * </ul>
 * 
 * @author Cowan
 */
public class PcpMmvWriter implements PcpWriter {
    private static enum TocType {
        INSTANCE_DOMAINS(1),
        INSTANCES(2),
        METRICS(3),
        VALUES(4),
        STRINGS(5);

        private final int identifier;

        private TocType(int identifier) {
            this.identifier = identifier;
        }
    }
    
    public static enum MmvFlag {
        MMV_FLAG_NOPREFIX(1),
        MMV_FLAG_PROCESS(2);
        
        private final int bitmask;

        MmvFlag(int bitmask) {
            this.bitmask = bitmask;
        }
        
        public int getBitmask() {
            return bitmask;
        }
    }

    private enum State {
        STOPPED,
        STARTING,
        STARTED
    }
    
    private static final Set<MmvFlag> DEFAULT_FLAGS = Collections.unmodifiableSet(EnumSet.of(
            MmvFlag.MMV_FLAG_NOPREFIX, MmvFlag.MMV_FLAG_PROCESS));


    private static final int HEADER_LENGTH = 40;
    private static final int TOC_LENGTH = 16;

    /**
     * The charset used for PCP metrics names and String values.
     */
    static final Charset PCP_CHARSET = Charset.forName("US-ASCII");
    private static final byte[] TAG = "MMV\0".getBytes(PCP_CHARSET);

    static final int DATA_VALUE_LENGTH = 16;

    private static final TypeHandler<String> MMV_STRING_HANDLER = new AbstractTypeHandler<String>(
            MmvMetricType.STRING, STRING_BLOCK_LENGTH) {
        @Override
        public boolean requiresLargeStorage() {
            return true;
        }

        @Override
        public void putBytes(ByteBuffer buffer, String value) {
            byte[] bytes = value.getBytes(PCP_CHARSET);
            int length = Math.min(bytes.length, STRING_BLOCK_LIMIT);
            buffer.put(bytes, 0, length);
            buffer.put((byte) 0);
        }
    };

    private final ByteBufferFactory byteBufferFactory;
    private final Store<PcpMetricInfo> metricInfoStore;
    private final Store<InstanceDomain> instanceDomainStore;
    private final MmvVersion mmvVersion;
    private final MetricNameValidator metricNameValidator;
    private final Map<MetricName, PcpValueInfo> metricData = Maps.newConcurrentMap();
    private final Map<Class<?>, TypeHandler<?>> typeHandlers = new ConcurrentHashMap<Class<?>, TypeHandler<?>>(
            DefaultTypeHandlers.getDefaultMappings());
    private final PcpStringStore stringStore = new PcpStringStore();
    private volatile State state = State.STOPPED;
    private final Monitor stateMonitor = new Monitor();
    private final Monitor.Guard isStarted = stateMonitor.newGuard(() -> state == State.STARTED);
    private volatile boolean usePerMetricLock = true;
    private final Map<PcpValueInfo,ByteBuffer> perMetricByteBuffers = newConcurrentMap();
    private final Object globalLock = new Object();
    @GuardedBy("itself")
    private volatile ByteBuffer dataFileBuffer = null;


    private File file = null;
    private volatile int processIdentifier = 0;
    private volatile int clusterIdentifier = 0;
    private volatile Set<MmvFlag> flags = DEFAULT_FLAGS; 
    
    /**
     * A new PcpMmvWriter using a simple default {@link IdentifierSourceSet}.
     * 
     * @see #PcpMmvWriter(File, IdentifierSourceSet)
     * @deprecated should pass in an explicit IdentifierSourceSet
     */
    @Deprecated
    public PcpMmvWriter(File file) {
        this(file, IdentifierSourceSet.DEFAULT_SET);
    }

    /**
     * Creates a new PcpMmvWriter writing to the underlying file, which will be created + opened as a
     * memory-mapped file. Uses the provided architecture to determine whether to write 32- or
     * 64-bit longs for some key header fields. Defaults to {@link MmvVersion#MMV_VERSION1} format
     * 
     * @param file
     *            the file to map
     * @param identifierSources
     *            the sources to use for coming up with identifiers for new metrics etc.
     */
    public PcpMmvWriter(File file, IdentifierSourceSet identifierSources) {
        this(file, identifierSources, MMV_VERSION1);
    }

    /**
     * Creates a new PcpMmvWriter writing to the underlying file, which will be created + opened as a
     * memory-mapped file. Uses the provided architecture to determine whether to write 32- or
     * 64-bit longs for some key header fields.
     *
     * @param file
     *            the file to map
     * @param identifierSources
     *            the sources to use for coming up with identifiers for new metrics etc.
     * @param mmvVersion
     *            the MMV version format to use
     */
    public PcpMmvWriter(File file, IdentifierSourceSet identifierSources, MmvVersion mmvVersion) {
        this(new FileByteBufferFactory(file), identifierSources, mmvVersion);
        this.file = file;
    }

    public PcpMmvWriter(ByteBufferFactory byteBufferFactory, IdentifierSourceSet identifierSources) {
        this(byteBufferFactory, identifierSources, MMV_VERSION1);
    }

    public PcpMmvWriter(ByteBufferFactory byteBufferFactory, IdentifierSourceSet identifierSources, MmvVersion mmvVersion) {
        this.byteBufferFactory = byteBufferFactory;
        this.metricInfoStore = mmvVersion.createMetricInfoStore(identifierSources, stringStore);
        this.instanceDomainStore = mmvVersion.createInstanceDomainStore(identifierSources, stringStore);
        this.mmvVersion = mmvVersion;
        this.metricNameValidator = mmvVersion.createMetricNameValidator();

        registerType(String.class, MMV_STRING_HANDLER);
    }

    /**
     * Creates a new PcpMmvWriter writing to the underlying file, which will be created + opened as a
     * memory-mapped file.  This is the constructor most people should use, unless you have a really
     * good reason not to.  It automatically handles all (incl. cross-platform) file location issues.
     * It will default to {@link MmvVersion#MMV_VERSION1} format
     * 
     * @param name
     *            logical name of instrumented subsystem (e.g. "hadoop")
     * @param identifierSources
     *            the sources to use for coming up with identifiers for new metrics etc.
     */
    public PcpMmvWriter(String name, IdentifierSourceSet identifierSources) {
        this(mmvFileFromName(name), identifierSources, MMV_VERSION1);
    }

    /**
     * Creates a new PcpMmvWriter writing to the underlying file, which will be created + opened as a
     * memory-mapped file.  This is the constructor most people should use, unless you have a really
     * good reason not to.  It automatically handles all (incl. cross-platform) file location issues.
     *
     * @param name
     *            logical name of instrumented subsystem (e.g. "hadoop")
     * @param identifierSources
     *            the sources to use for coming up with identifiers for new metrics etc.
     * @param mmvVersion
     *            the MMV version format to use
     */
    public PcpMmvWriter(String name, IdentifierSourceSet identifierSources, MmvVersion mmvVersion) {
        this(mmvFileFromName(name), identifierSources, mmvVersion);
    }

    private static File mmvFileFromName(String name) {
        Preconditions.checkArgument(!name.contains(File.separator), "MMV logical name must not contain path separators");

        File tmpDir = null;
        PcpConfig pcp = new PcpConfig();
        String pcpTemp = pcp.getValue("PCP_TMP_DIR");
        if (pcpTemp == null) {
            // PCP not installed, so create mapping in tmpdir
            tmpDir = new File(System.getProperty("java.io.tmpdir"));
        } else {
            tmpDir = new File(pcp.getRoot(), pcpTemp);
        }
        File mmvDir = new File(tmpDir, "mmv");
        return new File(mmvDir, name);
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
     * @see io.pcp.parfait.pcp.PcpWriter#registerType(java.lang.Class,
     * io.pcp.parfait.pcp.types.TypeHandler)
     */
    public final <T> void registerType(Class<T> runtimeClass, TypeHandler<T> handler) {
        if (state != State.STOPPED) {
            // Can't add any more metrics anyway; harmless
            return;
        }
        typeHandlers.put(runtimeClass, handler);
    }

    /*
     * (non-Javadoc)
     * @see io.pcp.parfait.pcp.PcpWriter#updateMetric(java.lang.String, java.lang.Object)
     */
    public final void updateMetric(MetricName name, Object value) {
        // If another thread has called start() we need to wait until the writer has completely started before
        // proceeding to update the metric value. This is to avoid a race condition where start() has already written
        // the old metric value, but has not yet finished writing all of the values, when the metric is updated. The
        // implementation here is a little complicated to avoid taking a lock on the happy paths.
        if (state == State.STARTED) {
            doUpdateMetric(name, value);
        } else if (state == State.STARTING) {
            if (stateMonitor.enterWhenUninterruptibly(isStarted, Duration.ofSeconds(10))) {
                // Leave the monitor immediately because we only care about being notified about the state change
                stateMonitor.leave();
                doUpdateMetric(name, value);
            }
        }
    }

    private void doUpdateMetric(MetricName name, Object value) {
        PcpValueInfo info = metricData.get(name);
        if (info == null) {
            throw new IllegalArgumentException("Metric " + name
                    + " was not added before initialising the writer");
        }
        updateValue(info, value);
    }

    /*
     * (non-Javadoc)
     * @see io.pcp.parfait.pcp.PcpWriter#start()
     */
    public final void start() throws IOException {
        updateState(State.STARTING);

        try {
            initialiseOffsets();

            dataFileBuffer = byteBufferFactory.build(getBufferLength());
            synchronized (globalLock) {
                populateDataBuffer(dataFileBuffer, metricData.values());
                preparePerMetricBufferSlices();
            }
        } catch (IOException | RuntimeException e) {
            updateState(State.STOPPED);
            throw e;
        }

        updateState(State.STARTED);
    }

    @Override
    public void reset() {
        updateState(State.STOPPED);
        metricData.clear();
        perMetricByteBuffers.clear();
        instanceDomainStore.clear();
        metricInfoStore.clear();
        stringStore.clear();
    }

    @Override
    public final void setInstanceDomainHelpText(String instanceDomain, String shortHelpText, String longHelpText) {
        InstanceDomain domain = getInstanceDomain(instanceDomain);
        domain.setHelpText(stringStore.createPcpString(shortHelpText), stringStore.createPcpString(longHelpText));
    }

    @Override
    public final void setMetricHelpText(String metricName, String shortHelpText, String longHelpText) {
        PcpMetricInfo info = getMetricInfo(metricName);
        if (!info.hasHelpText()) {
            info.setHelpText(stringStore.createPcpString(shortHelpText), stringStore.createPcpString(longHelpText));
        }
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


    public void setClusterIdentifier(int clusterIdentifier) {
        Preconditions.checkArgument((clusterIdentifier & 0xFFFFF000)==0, "ClusterIdentifier can only be a 12bit value");
    	this.clusterIdentifier = clusterIdentifier;
    }

    public void setProcessIdentifier(int pid) {
        Preconditions.checkArgument(pid > 0, "ProcessIdentifier can only be a positive integer");
        this.processIdentifier = pid;
    }

    public void setPerMetricLock(boolean usePerMetricLock) {
        Preconditions.checkState(state == State.STOPPED, "Cannot change use of perMetricLock when started");
        this.usePerMetricLock = usePerMetricLock;
    }

    public void setFlags(Set<MmvFlag> flags) {
        this.flags = EnumSet.copyOf(flags);
    }

    private synchronized void addMetricInfo(MetricName name, Semantics semantics, Unit<?> unit,
                                            Object initialValue, TypeHandler<?> pcpType) {
        if (metricData.containsKey(name)) {
            throw new IllegalArgumentException("Metric " + name
                    + " has already been added to writer");
        }
        metricNameValidator.validateNameConstraints(name);
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

        PcpValueInfo info = new PcpValueInfo(name, metricInfo, instance, initialValue,  stringStore);
        metricData.put(name, info);
    }

    private PcpMetricInfo getMetricInfo(String name) {
        return metricInfoStore.byName(name);
    }

    private InstanceDomain getInstanceDomain(String name) {
        return instanceDomainStore.byName(name);
    }

    private Collection<PcpMetricInfo> getMetricInfos() {
        return metricInfoStore.all();
    }



    private Collection<InstanceDomain> getInstanceDomains() {
        return instanceDomainStore.all();
    }

    private Collection<Instance> getInstances() {
        Collection<Instance> instances = new ArrayList<Instance>();
        for (InstanceDomain domain : instanceDomainStore.all()) {
            instances.addAll(domain.getInstances());
        }
        return instances;
    }

    private Collection<PcpValueInfo> getValueInfos() {
        return metricData.values();
    }

    private Collection<PcpString> getStrings() {
        return stringStore.getStrings();
    }

    @SuppressWarnings("unchecked")
    private void updateValue(PcpValueInfo info, Object value) {
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
        if (perMetricByteBuffer != null) {
            synchronized (perMetricByteBuffer) {
                perMetricByteBuffer.position(0);
                rawHandler.putBytes(perMetricByteBuffer, value);
            }
        }
    }

    private void writeValueWithGlobalLock(PcpValueInfo info, Object value, TypeHandler rawHandler) {
        synchronized (globalLock) {
            dataFileBuffer.position(rawHandler.requiresLargeStorage() ? info.getLargeValue()
                    .getOffset() : info.getOffset());
            rawHandler.putBytes(dataFileBuffer, value);
        }
    }

    private void populateDataBuffer(ByteBuffer dataFileBuffer, Collection<PcpValueInfo> valueInfos)
            throws IOException {

        // Automatically cleanup the file if this is a mapping where we
        // mandate PID checking from the MMV PMDA (MMV_FLAG_PROCESS) and
        // we were able to stash a path name earlier
        if (file != null && flags.contains(MmvFlag.MMV_FLAG_PROCESS)) {
            file.deleteOnExit();
        }

        dataFileBuffer.position(0);
        dataFileBuffer.put(TAG);
        dataFileBuffer.putInt(mmvVersion.getVersion());
        long generation = System.currentTimeMillis() / 1000;
        dataFileBuffer.putLong(generation);
        int gen2Offset = dataFileBuffer.position();
        // Generation 2 will be filled in later, once the file's ready
        dataFileBuffer.putLong(0);
        // 2 TOC blocks, 3 if there are instances
        dataFileBuffer.putInt(tocCount());
        dataFileBuffer.putInt(getFlagMask());
        dataFileBuffer.putInt(getProcessIdentifier());
        dataFileBuffer.putInt(clusterIdentifier);

        Collection<? extends MmvWritable> instanceDomains = getInstanceDomains();
        Collection<? extends MmvWritable> instances = getInstances();
        Collection<? extends MmvWritable> metrics = getMetricInfos();
        Collection<? extends MmvWritable> strings = getStrings();

        int tocBlockIndex = 0;

        if (!instanceDomains.isEmpty()) {
            dataFileBuffer.position(getTocOffset(tocBlockIndex++));
            writeToc(dataFileBuffer, TocType.INSTANCE_DOMAINS, instanceDomains.size(),
                    instanceDomains.iterator().next().getOffset());
        }

        if (!instances.isEmpty()) {
            dataFileBuffer.position(getTocOffset(tocBlockIndex++));
            writeToc(dataFileBuffer, TocType.INSTANCES, instances.size(), instances.iterator()
                    .next().getOffset());
        }

        dataFileBuffer.position(getTocOffset(tocBlockIndex++));

        int metricsFirstEntryOffset = metrics.isEmpty() ? 0 : metrics.iterator().next().getOffset();
        int valuesFirstEntryOffset = valueInfos.isEmpty() ? 0 : valueInfos.iterator().next().getOffset();


        writeToc(dataFileBuffer, TocType.METRICS, metrics.size(), metricsFirstEntryOffset);
        dataFileBuffer.position(getTocOffset(tocBlockIndex++));
        writeToc(dataFileBuffer, TocType.VALUES, valueInfos.size(), valuesFirstEntryOffset);


        if (!getStrings().isEmpty()) {
            dataFileBuffer.position(getTocOffset(tocBlockIndex++));
            writeToc(dataFileBuffer, TocType.STRINGS, strings.size(),
                    strings.iterator().next().getOffset());
        }

        for (MmvWritable instanceDomain : instanceDomains) {
            instanceDomain.writeToMmv(dataFileBuffer);
        }

        for (MmvWritable info : metrics) {
            info.writeToMmv(dataFileBuffer);
        }

        for (MmvWritable info : valueInfos) {
            info.writeToMmv(dataFileBuffer);
        }

        for (MmvWritable string : strings) {
            string.writeToMmv(dataFileBuffer);
        }

        // Once it's set up, let the agent know
        dataFileBuffer.position(gen2Offset);
        dataFileBuffer.putLong(generation);
    }

    private int getFlagMask() {
        int flagMask = 0;
        for (MmvFlag flag : flags) {
            flagMask |= flag.bitmask;
        }
        return flagMask;
    }

    private int getBufferLength() {
        return HEADER_LENGTH + (TOC_LENGTH * tocCount())
                + getByteSizeTotalFor(getInstanceDomains())
                + getByteSizeTotalFor(getInstances())
                + getByteSizeTotalFor(getMetricInfos())
                + getByteSizeTotalFor(getValueInfos())
                + getByteSizeTotalFor(getStrings());
    }

    private int getByteSizeTotalFor(Collection<? extends PcpOffset> offsettables) {
        int bytes = 0;
        for(PcpOffset offsetable : offsettables) {
            bytes += offsetable.byteSize();
        }
        return bytes;
    }

    /**
     * Writes out a PCP MMV table-of-contents block.
     * 
     * @param dataFileBuffer
     *            ByteBuffer positioned at the correct offset in the file for the block
     * @param tocType
     *            the type of TOC block to write
     * @param entryCount
     *            the number of blocks of type tocType to be found in the file
     * @param firstEntryOffset
     *            the offset of the first tocType block, relative to start of the file
     */
    private void writeToc(ByteBuffer dataFileBuffer, TocType tocType, int entryCount,
            int firstEntryOffset) {
        dataFileBuffer.putInt(tocType.identifier);
        dataFileBuffer.putInt(entryCount);
        dataFileBuffer.putLong(firstEntryOffset);
    }

    /**
     * Calculates the file offset of a given PCP MMV TOC block
     * 
     * @param tocIndex
     *            the 0-based index of the TOC block to be written
     * @return the file offset used to store that TOC block (32-bit regardless of architecture)
     */
    private int getTocOffset(int tocIndex) {
        return HEADER_LENGTH + (tocIndex * TOC_LENGTH);
    }

    private synchronized void initialiseOffsets() {
        int nextOffset = HEADER_LENGTH + (TOC_LENGTH * tocCount());

        nextOffset = initializeOffsets(getInstanceDomains(), nextOffset);
        nextOffset = initializeOffsets(getInstances(), nextOffset);
        nextOffset = initializeOffsets(getMetricInfos(), nextOffset);
        nextOffset = initializeOffsets(getValueInfos(), nextOffset);
        initializeOffsets(getStrings(), nextOffset);
    }

	private int initializeOffsets(Collection<? extends PcpOffset> offsettables, int nextOffset) {
        for (PcpOffset offsettable : offsettables) {
            offsettable.setOffset(nextOffset);
            nextOffset += offsettable.byteSize();
        }
        return nextOffset;
    }

    private int tocCount() {
        int tocCount = 2; // metrics + values
        if (!getInstances().isEmpty()) {
            tocCount += 2;
        }
        if (!getStrings().isEmpty()) {
            tocCount++;
        }
        return tocCount;
    }

    /**
     * @return the PID of the current running Java Process, or a proxied PID if requested.
     */
    private int getProcessIdentifier() {
        if (processIdentifier == 0) {
            String processName = ManagementFactory.getRuntimeMXBean().getName();
            processIdentifier = Integer.valueOf(processName.split("@")[0]);
        }
        return processIdentifier;
    }

    private void updateState(State newState) {
        stateMonitor.enter();
        try {
            state = newState;
        } finally {
            stateMonitor.leave();
        }
    }

    public static void main(String[] args) throws IOException {
        PcpMmvWriter bridge;
        
        if (args.length == 0) {
            // use $PCP_PMDAS_DIR/mmv/mmvdump (no args) as diagnostic tool
            bridge = new PcpMmvWriter("test", IdentifierSourceSet.DEFAULT_SET);
        }
        else {
            bridge = new PcpMmvWriter(new File(args[0]), IdentifierSourceSet.DEFAULT_SET);        
        }

        // Automatically uses default int handler
        bridge.addMetric(MetricName.parse("sheep[baabaablack].bagsfull.count"), Semantics.COUNTER,
                ONE.multiply(1000), 3);

        // Automatically uses default boolean-to-int handler
        bridge.addMetric(MetricName.parse("sheep[baabaablack].bagsfull.haveany"),
                Semantics.INSTANT, null, new AtomicBoolean(true));
        bridge.addMetric(MetricName.parse("sheep[limpy].bagsfull.haveany"), Semantics.INSTANT,
                null, new AtomicBoolean(false));

        // Automatically uses default long handler
        bridge.addMetric(MetricName.parse("sheep[insomniac].jumps"), Semantics.COUNTER, ONE,
                12345678901234L);

        // Automatically uses default double handler
        bridge.addMetric(MetricName.parse("sheep[limpy].legs.available"), Semantics.DISCRETE,
                ONE, 0.75);

        // Uses this class' custom String handler
        bridge.addMetric(MetricName.parse("sheep[limpy].jumpitem"), Semantics.DISCRETE, null,
                "fence");

        // addMetric(GregorianCalendar) would fail, as there's no handler registered by default for
        // GregorianCalendars; use a custom one which puts the year as an int
        bridge.addMetric(MetricName.parse("sheep[insomniac].lastjumped"), Semantics.INSTANT, null,
                new GregorianCalendar(), new AbstractTypeHandler<GregorianCalendar>(
                        MmvMetricType.I32, 4) {
                    public void putBytes(ByteBuffer buffer, GregorianCalendar value) {
                        buffer.putInt(value.get(GregorianCalendar.YEAR));
                    }
                });

        // addMetric(Date) would fail, as there's no handler registered; register one for all date
        // types from now on
        bridge.registerType(Date.class, new AbstractTypeHandler<Date>(MmvMetricType.I64, 8) {
            public void putBytes(ByteBuffer buffer, Date value) {
                buffer.putLong(value.getTime());
            }
        });
        // These will both use the handler we just registered
        bridge.addMetric(MetricName.parse("cow.how.now"), Semantics.INSTANT, null, new Date());
        bridge.addMetric(MetricName.parse("cow.how.then"), Semantics.INSTANT, null,
                new GregorianCalendar(1990, 1, 1, 12, 34, 56).getTime());

        // Uses units
        bridge.addMetric(MetricName.parse("cow.bytes.total"), Semantics.COUNTER,
                BYTE, 10000001);
        bridge.addMetric(MetricName.parse("cow.bytes.rate"), Semantics.INSTANT,
                BYTE.multiply(1024).divide(SECOND), new Date());
        bridge.addMetric(MetricName.parse("cow.bytes.chewtime"), Semantics.INSTANT,
                HOUR.divide(BYTE), 7);
        bridge.addMetric(MetricName.parse("cow.bytes.jawmotion"), Semantics.INSTANT,
                KILO(HERTZ), 0.5);

        // Set up some help text
        bridge.setInstanceDomainHelpText(
                        "sheep",
                        "sheep in the paddock",
                        "List of all the sheep in the paddock. Includes 'baabaablack', 'insomniac' (who likes to jump fences), and 'limpy' the three-legged wonder sheep.");
        bridge.setMetricHelpText("sheep.jumps", "# of jumps done",
                "Number of times the sheep has jumped over its jumpitem");

        // All the metrics are added; write the file
        bridge.start();
        // Metrics are visible to the agent from this point on

        // Sold a bag! Better update the count
        bridge.updateMetric(MetricName.parse("sheep[baabaablack].bagsfull.count"), 2);
        // The fence broke! Need something new to jump over
        bridge.updateMetric(MetricName.parse("sheep[limpy].jumpitem"), "Honda Civic");
        // Values will be reflected in the agent immediately
    }

    static abstract class Store<T extends PcpId> {
        private final Map<String, T> byName = new LinkedHashMap<String, T>();
        private final Map<Integer, T> byId = new LinkedHashMap<Integer, T>();
        final IdentifierSource identifierSource;

        Store(IdentifierSource source) {
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

        synchronized void clear() {
            byId.clear();
            byName.clear();
        }
    }

    @Override
    public String toString() {
        return "PcpMmvWriter[byteBufferFactory=" + byteBufferFactory + ']';
    }
}
