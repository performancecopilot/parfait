package com.custardsource.parfait.dxm;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicBoolean;

import com.custardsource.parfait.dxm.types.AbstractTypeHandler;
import com.custardsource.parfait.dxm.types.DefaultTypeHandlers;
import com.custardsource.parfait.dxm.types.MmvMetricType;
import com.custardsource.parfait.dxm.types.TypeHandler;

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
 * <li>String types are not supported (version 0 of the MMV protocol does not support string types)</li>
 * <li>Instance domains are not supported (to be added later)</li>
 * <li>Dimensions are not specified (to be added later)</li>
 * <li>Receiving agent must be using MMV agent version 2.8.10 or later (prior versions contained an
 * ambiguity in the file format which may lead to indeterminate behaviour depending on
 * sizeof(time_t))</li>
 * </ul>
 * 
 * @author Cowan
 */
public class PcpMmvWriter extends BasePcpWriter {
    private static enum TocType {
        INSTANCES(1),
        METRICS(2),
        VALUES(3);

        private final int identifier;

        private TocType(int identifier) {
            this.identifier = identifier;
        }
    }

    /**
     * The maximum length of a metric name able to be exported to the MMV agent. Note that this is
     * relative to {@link #PCP_CHARSET} (it's a measure of the maximum number of bytes, not the Java
     * String length)
     */
    public static final int METRIC_NAME_LIMIT = 63;
    /**
     * The maximum length of an instance name able to be exported to the MMV agent. Note that this is
     * relative to {@link #PCP_CHARSET} (it's a measure of the maximum number of bytes, not the Java
     * String length)
     */
    public static final int INSTANCE_NAME_LIMIT = 63;

    private static final int HEADER_LENGTH = 32;
    private static final int TOC_LENGTH = 16;
    private static final int METRIC_LENGTH = 80;
    private static final int VALUE_LENGTH = 32;
    private static final int DEFAULT_INSTANCE_DOMAIN_ID = -1;
    private static final int INSTANCE_LENGTH = 68;

	/**
	 * The charset used for PCP metrics names and String values.
	 */
	public static final Charset PCP_CHARSET = Charset.forName("US-ASCII");
    private static final byte[] TAG = "MMV\0".getBytes(PCP_CHARSET);
    private static final int MMV_FORMAT_VERSION = 1;

	private static final int DATA_VALUE_OFFSET_WITHIN_BLOCK = 16;


    /**
     * Creates a new PcpMmvFile writing to the underlying file, which will be created + opened as a
     * memory-mapped file. Uses the provided architecture to determine whether to write 32- or
     * 64-bit longs for some key header fields.
     * 
     * @param file
     *            the file to map
     */
    public PcpMmvWriter(File file) {
    	super(file);
    }

    @Override
    protected void populateDataBuffer(ByteBuffer dataFileBuffer, Collection<PcpValueInfo> valueInfos) throws IOException {
    	int metricCount = valueInfos.size();

        dataFileBuffer.position(0);
        dataFileBuffer.put(TAG);
        dataFileBuffer.putInt(MMV_FORMAT_VERSION);
        long generation = System.currentTimeMillis() / 1000;
        dataFileBuffer.putLong(generation);
        int gen2Offset = dataFileBuffer.position();
        // Generation 2 will be filled in later, once the file's ready
        dataFileBuffer.putLong(0);

        Collection<Instance> instances = getInstances();
        Collection<PcpMetricInfo> metrics = getMetricInfos();
       
        // 2 TOC blocks, 3 if there are instances
        dataFileBuffer.putInt(tocCount());
        
        PcpMetricInfo firstMetric = metrics.iterator().next();
        PcpValueInfo firstValue = valueInfos.iterator().next();
        
		dataFileBuffer.position(getTocOffset(0));
		writeToc(dataFileBuffer, TocType.METRICS, metricCount, firstMetric.getOffset());
		dataFileBuffer.position(getTocOffset(1));
		writeToc(dataFileBuffer, TocType.VALUES, metricCount, firstValue
				.getOffsets().dataBlockOffset());
		
		if (!instances.isEmpty()) {
	        dataFileBuffer.position(getTocOffset(2));
            writeToc(dataFileBuffer, TocType.INSTANCES, instances.size(), instances.iterator()
                    .next().getOffset());
		}

        for (PcpMetricInfo info : metrics) {
            dataFileBuffer.position(info.getOffset());
            writeMetricsSection(dataFileBuffer, info, info.getTypeHandler().getMetricType());
        }

        for (Instance instance : instances) {
            dataFileBuffer.position(instance.getOffset());
            writeInstanceSection(dataFileBuffer, instance);
        }
		
        for (PcpValueInfo info : valueInfos) {
            dataFileBuffer.position(info.getOffsets().dataBlockOffset());
            writeValueSection(dataFileBuffer, info.getDescriptorOffset(), info.getInitialValue(),
                    info.getTypeHandler(), info.getInstanceOffset());
        }

        // Once it's set up, let the agent know
        dataFileBuffer.position(gen2Offset);
        dataFileBuffer.putLong(generation);
	}

    @Override
    protected int getFileLength() {
        int metricCount = getMetricInfos().size();
        int instanceCount = getInstances().size();
        int valueCount = getValueInfos().size();
        int tocCount = tocCount();
        return HEADER_LENGTH + (TOC_LENGTH * tocCount) + (INSTANCE_LENGTH * instanceCount)
                + (METRIC_LENGTH * metricCount) + (VALUE_LENGTH * valueCount);
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
     *            the info of the metric (name must be &le; {@link #METRIC_NAME_LIMIT} characters, and
     *            must be convertible to {@link #PCP_CHARSET})
     * @param metricType
     *            the type of the metric
     */
    private void writeMetricsSection(ByteBuffer dataFileBuffer, PcpMetricInfo info,
            MmvMetricType metricType) {
        int originalPosition = dataFileBuffer.position();

        dataFileBuffer.put(info.getMetricName().getBytes(PCP_CHARSET));
        dataFileBuffer.put((byte) 0);
        dataFileBuffer.position(originalPosition + METRIC_NAME_LIMIT + 1);
        dataFileBuffer.putInt(metricType.getIdentifier());
        if (info.getInstanceDomain() != null) {
            dataFileBuffer.putInt(info.getInstanceDomain().getId());
        } else {
            dataFileBuffer.putInt(DEFAULT_INSTANCE_DOMAIN_ID);
        }
        // Dimensions not yet supported
        dataFileBuffer.putInt(0);
        // Semantics not yet supported
        dataFileBuffer.putInt(0);
    }

    /**
     * Writes the value block for an individual metric to the file.
     * 
     * @param dataFileBuffer
     *            ByteBuffer positioned at the correct offset in the file for the block
     * @param descriptorOffset
     *            the offset of the descriptor block of this metric
     * @param value
     *            the value to be written to the file
     * @param handler
     *            the {@link TypeHandler} to use to convert the value to bytes
     * @param instanceOffset
     *            the offset of the instance in the file
     */
    @SuppressWarnings("unchecked")
	private void writeValueSection(ByteBuffer dataFileBuffer,
			int descriptorOffset, Object value, TypeHandler<?> handler, int instanceOffset) {
        dataFileBuffer.putLong(descriptorOffset);
        dataFileBuffer.putLong(instanceOffset);
        TypeHandler rawHandler = handler;
        rawHandler.putBytes(dataFileBuffer, value);
    }

    private void writeInstanceSection(ByteBuffer dataFileBuffer, Instance instance) {
        dataFileBuffer.putInt(instance.getId());
        dataFileBuffer.put(instance.getName().getBytes(PCP_CHARSET));
        dataFileBuffer.put((byte) 0);
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
        for (Instance instance : getInstances()) {
            instance.setOffset(nextOffset);
            nextOffset += INSTANCE_LENGTH;
        }

        for (PcpMetricInfo metric : getMetricInfos()) {
            metric.setOffset(nextOffset);
            nextOffset += METRIC_LENGTH;
        }

        for (PcpValueInfo value : getValueInfos()) {
            value.setOffsets(new PcpOffset(nextOffset, nextOffset
                    + DATA_VALUE_OFFSET_WITHIN_BLOCK));
            nextOffset += VALUE_LENGTH;

        }
    }

	private int tocCount() {
        return getInstances().isEmpty() ? 2 : 3;
    }

    public static void main(String[] args) throws IOException {
        PcpMmvWriter instanceBridge = new PcpMmvWriter(new File("/var/tmp/mmv/nodots"));
        instanceBridge.addMetric(MetricName.parse("sheep[baabaablack]"), 3);
        instanceBridge.addMetric(MetricName.parse("sheep[shaun]"), 0);
        instanceBridge.start();
        instanceBridge.updateMetric(MetricName.parse("sheep[baabaablack]"), 2);
 
        instanceBridge = new PcpMmvWriter(new File("/var/tmp/mmv/instances"));
        instanceBridge.addMetric(MetricName.parse("sheep[baabaablack].bagsfull"), 3);
        instanceBridge.addMetric(MetricName.parse("sheep[shaun].bagsfull"), 0);
        instanceBridge.start();
        instanceBridge.updateMetric(MetricName.parse("sheep[baabaablack].bagsfull"), 2);
               
        
        PcpMmvWriter bridge = new PcpMmvWriter(new File("/var/tmp/mmv/mmvtest2"));
        // Uses default boolean-to-int handler
        bridge.addMetric(MetricName.parse("sheep.baabaablack.bagsfull.haveany"), new AtomicBoolean(true));
        // Uses default int handler
        bridge.addMetric(MetricName.parse("sheep.baabaablack.bagsfull.count"), 3);
        // Uses default long handler
        bridge.addMetric(MetricName.parse("sheep.insomniac.count"), 12345678901234L);
        // Uses default double handler
        bridge.addMetric(MetricName.parse("sheep.limpy.legs.available"), 0.75);
        // addMetric(String) would fail, as there's no handler registered; use a custom one which
        // puts the string's length as an int
        bridge.addMetric(MetricName.parse("sheep.insomniac.jumpitem"), "Fence", new AbstractTypeHandler<String>(
                MmvMetricType.I32, 4) {
            public void putBytes(ByteBuffer buffer, String value) {
                buffer.putInt(value.length());
            }
        });
        // addMetric(Date) would fail, as there's no handler registered; register one for all date
        // types from now on
        bridge.registerType(Date.class, new AbstractTypeHandler<Date>(MmvMetricType.I64, 8) {
            public void putBytes(ByteBuffer buffer, Date value) {
                buffer.putLong(value.getTime());
            }
        });
        bridge.addMetric(MetricName.parse("cow.how.now"), new Date());
        bridge.addMetric(MetricName.parse("cow.how.then"), new GregorianCalendar(1990, 1, 1, 12, 34, 56).getTime());
        bridge.start();
        // Sold a bag
        bridge.updateMetric(MetricName.parse("sheep.baabaablack.bagsfull.count"), 2);
    }
}