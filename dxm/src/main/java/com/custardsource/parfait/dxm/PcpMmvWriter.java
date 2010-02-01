package com.custardsource.parfait.dxm;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.custardsource.parfait.dxm.semantics.Semantics;
import com.custardsource.parfait.dxm.semantics.UnitMapping;
import com.custardsource.parfait.dxm.types.AbstractTypeHandler;
import com.custardsource.parfait.dxm.types.DefaultTypeHandlers;
import com.custardsource.parfait.dxm.types.MmvMetricType;
import com.custardsource.parfait.dxm.types.TypeHandler;
import com.google.common.base.Preconditions;

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
 * <li>{@link #addMetric(MetricName, Object)} for every metric which will be monitored by the file
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
 * <li>Process ID is obtained in a Sun HotSpot-JVM specific way (likely to work on other JVMs but
 * not guaranteed)</li>
 * <li>Receiving agent must be using MMV agent version 2.8.10 or later (version 1 of the MMV on-disk
 * format)</li>
 * </ul>
 * 
 * @author Cowan
 */
public class PcpMmvWriter extends BasePcpWriter {
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
    
    public static final Set<MmvFlag> DEFAULT_FLAGS = Collections.unmodifiableSet(EnumSet.of(
            MmvFlag.MMV_FLAG_NOPREFIX, MmvFlag.MMV_FLAG_PROCESS));

    /**
     * The maximum length of a metric name able to be exported to the MMV agent. Note that this is
     * relative to {@link #PCP_CHARSET} (it's a measure of the maximum number of bytes, not the Java
     * String length)
     */
    public static final int METRIC_NAME_LIMIT = 63;
    /**
     * The maximum length of an instance name able to be exported to the MMV agent. Note that this
     * is relative to {@link #PCP_CHARSET} (it's a measure of the maximum number of bytes, not the
     * Java String length)
     */
    public static final int INSTANCE_NAME_LIMIT = 63;

    private static final int HEADER_LENGTH = 40;
    private static final int TOC_LENGTH = 16;
    private static final int METRIC_LENGTH = 104;
    private static final int VALUE_LENGTH = 32;
    private static final int DEFAULT_INSTANCE_DOMAIN_ID = -1;
    private static final int INSTANCE_LENGTH = 80;
    private static final int INSTANCE_DOMAIN_LENGTH = 32;
    private static final int STRING_BLOCK_LENGTH = 256;

    /**
     * The charset used for PCP metrics names and String values.
     */
    public static final Charset PCP_CHARSET = Charset.forName("US-ASCII");
    private static final byte[] TAG = "MMV\0".getBytes(PCP_CHARSET);
    private static final int MMV_FORMAT_VERSION = 1;

    private static final int DATA_VALUE_LENGTH = 16;

    private static final TypeHandler<String> MMV_STRING_HANDLER = new AbstractTypeHandler<String>(
            MmvMetricType.STRING, STRING_BLOCK_LENGTH) {
        @Override
        public boolean requiresLargeStorage() {
            return true;
        }

        @Override
        public void putBytes(ByteBuffer buffer, String value) {
            byte[] bytes = value.getBytes(PCP_CHARSET);
            int length = Math.min(bytes.length, STRING_BLOCK_LENGTH - 1);
            buffer.put(bytes, 0, length);
            buffer.put((byte) 0);
        }
    };

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
     * 64-bit longs for some key header fields.
     * 
     * @param file
     *            the file to map
     * @param identifierSources
     *            the sources to use for coming up with identifiers for new metrics etc.
     */
    public PcpMmvWriter(File file, IdentifierSourceSet identifierSources) {
        super(file, identifierSources);
        registerType(String.class, MMV_STRING_HANDLER);
    }

    public void setClusterIdentifier(int clusterIdentifier) {
    	this.clusterIdentifier = clusterIdentifier;
    }
    
    public void setFlags(Set<MmvFlag> flags) {
        this.flags = EnumSet.copyOf(flags);
    }
    
    @Override
    protected void populateDataBuffer(ByteBuffer dataFileBuffer, Collection<PcpValueInfo> valueInfos)
            throws IOException {
        dataFileBuffer.position(0);
        dataFileBuffer.put(TAG);
        dataFileBuffer.putInt(MMV_FORMAT_VERSION);
        long generation = System.currentTimeMillis() / 1000;
        dataFileBuffer.putLong(generation);
        int gen2Offset = dataFileBuffer.position();
        // Generation 2 will be filled in later, once the file's ready
        dataFileBuffer.putLong(0);
        // 2 TOC blocks, 3 if there are instances
        dataFileBuffer.putInt(tocCount());
        dataFileBuffer.putInt(getFlagMask());
        dataFileBuffer.putInt(getPid());
        dataFileBuffer.putInt(clusterIdentifier);

        Collection<InstanceDomain> instanceDomains = getInstanceDomains();
        Collection<Instance> instances = getInstances();
        Collection<PcpMetricInfo> metrics = getMetricInfos();
        Collection<PcpString> strings = getStrings();

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
        writeToc(dataFileBuffer, TocType.METRICS, metrics.size(), metrics.iterator().next()
                .getOffset());
        dataFileBuffer.position(getTocOffset(tocBlockIndex++));
        writeToc(dataFileBuffer, TocType.VALUES, valueInfos.size(), valueInfos.iterator().next()
                .getOffset());

        if (!getStrings().isEmpty()) {
            dataFileBuffer.position(getTocOffset(tocBlockIndex++));
            writeToc(dataFileBuffer, TocType.STRINGS, strings.size(),
                    strings.iterator().next().getOffset());
        }

        for (InstanceDomain instanceDomain : instanceDomains) {
            dataFileBuffer.position(instanceDomain.getOffset());
            writeInstanceDomainSection(dataFileBuffer, instanceDomain);
            for (Instance instance : instanceDomain.getInstances()) {
                dataFileBuffer.position(instance.getOffset());
                writeInstanceSection(dataFileBuffer, instance);
            }
        }

        for (PcpMetricInfo info : metrics) {
            dataFileBuffer.position(info.getOffset());
            writeMetricsSection(dataFileBuffer, info, info.getTypeHandler().getMetricType());
        }

        for (PcpValueInfo info : valueInfos) {
            dataFileBuffer.position(info.getOffset());
            writeValueSection(dataFileBuffer, info);
        }

        for (PcpString string : strings) {
            dataFileBuffer.position(string.getOffset());
            writeStringSection(dataFileBuffer, string.getInitialValue());
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

    private void writeStringSection(ByteBuffer dataFileBuffer, String value) {
    	byte[] bytes = value.getBytes(PCP_CHARSET);
    	Preconditions.checkArgument(bytes.length < STRING_BLOCK_LENGTH);
        dataFileBuffer.put(bytes);
        dataFileBuffer.put((byte) 0);
    }

    @Override
    protected int getFileLength() {
        int instanceDomainCount = getInstanceDomains().size();
        int metricCount = getMetricInfos().size();
        int instanceCount = getInstances().size();
        int valueCount = getValueInfos().size();
        int tocCount = tocCount();
        int stringCount = getStrings().size();
        return HEADER_LENGTH + (TOC_LENGTH * tocCount)
                + (INSTANCE_DOMAIN_LENGTH * instanceDomainCount)
                + (INSTANCE_LENGTH * instanceCount) + (METRIC_LENGTH * metricCount)
                + (VALUE_LENGTH * valueCount) + (STRING_BLOCK_LENGTH * stringCount);
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
     * Writes the descriptor block for an individual metric to the file.
     * 
     * @param dataFileBuffer
     *            ByteBuffer positioned at the correct offset in the file for the block
     * @param info
     *            the info of the metric (name must be &le; {@link #METRIC_NAME_LIMIT} characters,
     *            and must be convertible to {@link #PCP_CHARSET})
     * @param metricType
     *            the type of the metric
     */
    private void writeMetricsSection(ByteBuffer dataFileBuffer, PcpMetricInfo info,
            MmvMetricType metricType) {
        int originalPosition = dataFileBuffer.position();

        dataFileBuffer.put(info.getMetricName().getBytes(PCP_CHARSET));
        dataFileBuffer.put((byte) 0);
        dataFileBuffer.position(originalPosition + METRIC_NAME_LIMIT + 1);
        dataFileBuffer.putInt(info.getId());
        dataFileBuffer.putInt(metricType.getIdentifier());
        dataFileBuffer.putInt(info.getSemantics().getPcpValue());
        dataFileBuffer.putInt(UnitMapping.getDimensions(info.getUnit(), info.getMetricName()));
        if (info.getInstanceDomain() != null) {
            dataFileBuffer.putInt(info.getInstanceDomain().getId());
        } else {
            dataFileBuffer.putInt(DEFAULT_INSTANCE_DOMAIN_ID);
        }
        // Just padding
        dataFileBuffer.putInt(0);
        dataFileBuffer.putLong(getStringOffset(info.getShortHelpText()));
        dataFileBuffer.putLong(getStringOffset(info.getLongHelpText()));
    }

    /**
     * Writes the value block for an individual metric to the file.
     * 
     * @param dataFileBuffer
     *            ByteBuffer positioned at the correct offset in the file for the block
     * @param value
     *            the PcpValueInfo to be written to the file
     */
    @SuppressWarnings("unchecked")
    private void writeValueSection(ByteBuffer dataFileBuffer, PcpValueInfo info) {
        int originalPosition = dataFileBuffer.position();
        TypeHandler rawHandler = info.getTypeHandler();
        if (rawHandler.requiresLargeStorage()) {
            // API requires the length here but it's currently unused -- write out the maximum
            // possible length
            dataFileBuffer.putLong(STRING_BLOCK_LENGTH - 1);
            dataFileBuffer.putLong(info.getLargeValue().getOffset());
            dataFileBuffer.position(info.getLargeValue().getOffset());
        }
        rawHandler.putBytes(dataFileBuffer, info.getInitialValue());
        dataFileBuffer.position(originalPosition + DATA_VALUE_LENGTH);
        dataFileBuffer.putLong(info.getDescriptorOffset());
        dataFileBuffer.putLong(info.getInstanceOffset());
    }

    private void writeInstanceSection(ByteBuffer dataFileBuffer, Instance instance) {
        dataFileBuffer.putLong(instance.getInstanceDomain().getOffset());
        dataFileBuffer.putInt(0);
        dataFileBuffer.putInt(instance.getId());
        dataFileBuffer.put(instance.getName().getBytes(PCP_CHARSET));
    }

    private void writeInstanceDomainSection(ByteBuffer dataFileBuffer, InstanceDomain instanceDomain) {
        dataFileBuffer.putInt(instanceDomain.getId());
        dataFileBuffer.putInt(instanceDomain.getInstanceCount());
        dataFileBuffer.putLong(instanceDomain.getFirstInstanceOffset());
        dataFileBuffer.putLong(getStringOffset(instanceDomain.getShortHelpText()));
        dataFileBuffer.putLong(getStringOffset(instanceDomain.getLongHelpText()));
    }

    private long getStringOffset(PcpString text) {
        if (text == null) {
            return 0;
        }
        return text.getOffset();
    }

    /**
     * Calculates the file offset of a given PCP MMV TOC block
     * 
     * @param tocIndex
     *            the 0-based index of the TOC block to be written
     * @return the file offset used to store that TOC block (32-bit regardless of architecture)
     */
    private final int getTocOffset(int tocIndex) {
        return HEADER_LENGTH + (tocIndex * TOC_LENGTH);
    }

    @Override
    protected Charset getCharset() {
        return PCP_CHARSET;
    }

    @Override
    protected int getMetricNameLimit() {
        return METRIC_NAME_LIMIT;
    }

    @Override
    protected int getInstanceNameLimit() {
        return INSTANCE_NAME_LIMIT;
    }

    @Override
    protected synchronized void initialiseOffsets() {
        int nextOffset = HEADER_LENGTH + (TOC_LENGTH * tocCount());

        nextOffset = initializeOffsets(getInstanceDomains(), nextOffset, INSTANCE_DOMAIN_LENGTH);
        nextOffset = initializeOffsets(getInstances(), nextOffset, INSTANCE_LENGTH);
        nextOffset = initializeOffsets(getMetricInfos(), nextOffset, METRIC_LENGTH);
        nextOffset = initializeOffsets(getValueInfos(), nextOffset, VALUE_LENGTH);
        initializeOffsets(getStrings(), nextOffset, STRING_BLOCK_LENGTH);
    }

	private int initializeOffsets(Collection<? extends PcpOffset> offsettables,
			int nextOffset, int blockLength) {
        for (PcpOffset offsettable : offsettables) {
            offsettable.setOffset(nextOffset);
            nextOffset += blockLength;
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
     * @return the PID of the current running Java Process
     */
    private int getPid() {
        String processIdentifier = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.valueOf(processIdentifier.split("@")[0]);
    }

    public static void main(String[] args) throws IOException {
		final String output = args.length == 0 ? "/var/tmp/mmv/mmvtest"
				: args[0];
		PcpMmvWriter bridge = new PcpMmvWriter(new File(output),
				IdentifierSourceSet.DEFAULT_SET);

        // Automatically uses default int handler
        bridge.addMetric(MetricName.parse("sheep[baabaablack].bagsfull.count"), Semantics.COUNTER,
                Unit.ONE.times(1000), 3);

        // Automatically uses default boolean-to-int handler
        bridge.addMetric(MetricName.parse("sheep[baabaablack].bagsfull.haveany"),
                Semantics.INSTANT, null, new AtomicBoolean(true));
        bridge.addMetric(MetricName.parse("sheep[limpy].bagsfull.haveany"), Semantics.INSTANT,
                null, new AtomicBoolean(false));

        // Automatically uses default long handler
        bridge.addMetric(MetricName.parse("sheep[insomniac].jumps"), Semantics.COUNTER, Unit.ONE,
                12345678901234L);

        // Automatically uses default double handler
        bridge.addMetric(MetricName.parse("sheep[limpy].legs.available"), Semantics.DISCRETE,
                Unit.ONE, 0.75);

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
        bridge.addMetric(MetricName.parse("cow.launch.velocity"), Semantics.INSTANT, NonSI.MILE
                .divide(SI.SECOND), new Date());
        bridge.addMetric(MetricName.parse("cow.bytes.total"), Semantics.COUNTER, NonSI.BYTE,
                10000001);
        bridge.addMetric(MetricName.parse("cow.bytes.rate"), Semantics.INSTANT, NonSI.BYTE.times(
                1024).divide(SI.SECOND), new Date());
        bridge.addMetric(MetricName.parse("cow.bytes.chewtime"), Semantics.INSTANT, NonSI.HOUR
                .divide(NonSI.BYTE), 7);
        bridge.addMetric(MetricName.parse("cow.bytes.jawmotion"), Semantics.INSTANT, SI
                .KILO(SI.HERTZ), 0.5);

        // Set up some help text
        bridge
                .setInstanceDomainHelpText(
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
}