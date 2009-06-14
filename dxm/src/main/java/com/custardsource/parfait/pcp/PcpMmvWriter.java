package com.custardsource.parfait.pcp;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicBoolean;

import com.custardsource.parfait.pcp.types.AbstractTypeHandler;
import com.custardsource.parfait.pcp.types.DefaultTypeHandlers;
import com.custardsource.parfait.pcp.types.MmvMetricType;
import com.custardsource.parfait.pcp.types.TypeHandler;

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
 * <li>{@link #addMetric(String, Object)} for every metric which will be monitored by the file
 * <li>{@link #start()} to write out the file ready for the MMV agent to read
 * <li>{@link #updateMetric(String, Object)} metrics as new values come to hand
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
 * <li>Receiving agent must be using MMV agent version 2.8.9 or later (prior versions contained an
 * ambiguity in the file format which may lead to indeterminate behaviour depending on
 * sizeof(time_t))</li>
 * </ul>
 * 
 * @author Cowan
 */
public class PcpMmvWriter extends BasePcpWriter {
    private static enum TocType {
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

    private static final int HEADER_LENGTH = 32;
    private static final int TOC_LENGTH = 16;
    private static final int METRIC_LENGTH = 76;
    private static final int VALUE_LENGTH = 24;
    private static final int DEFAULT_INSTANCE_DOMAIN_ID = -1;

	/**
	 * The charset used for PCP metrics names and String values.
	 */
	public static final Charset PCP_CHARSET = Charset.forName("US-ASCII");
    private static final byte[] TAG = "MMV\0".getBytes(PCP_CHARSET);
    private static final int MMV_FORMAT_VERSION = 0;

	private static final int DATA_VALUE_OFFSET_WITHIN_BLOCK = 8;

    // @GuardedBy(this)
	private boolean firstOffsetCalculated = false;
    // @GuardedBy(this)
	private int nextDescriptorOffset;
    // @GuardedBy(this)
	private int nextDataBlockOffset;
    // @GuardedBy(this)
	private int nextDataValueOffset;

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
    protected void populateDataBuffer(ByteBuffer dataFileBuffer, Collection<PcpMetricInfo> metricInfos) throws IOException {
    	int metricCount = metricInfos.size();

        dataFileBuffer.position(0);
        dataFileBuffer.put(TAG);
        dataFileBuffer.putInt(MMV_FORMAT_VERSION);
        long generation = System.currentTimeMillis() / 1000;
        dataFileBuffer.putLong(generation);
        int gen2Offset = dataFileBuffer.position();
        // Generation 2 will be filled in later, once the file's ready
        dataFileBuffer.putLong(0);

        // 2 TOC blocks;
        dataFileBuffer.putInt(2);

        PcpMetricInfo first = metricInfos.iterator().next();
        
		dataFileBuffer.position(getTocOffset(0));
		writeToc(dataFileBuffer, TocType.METRICS, metricCount, first
				.getOffsets().descriptorOffset());
		dataFileBuffer.position(getTocOffset(1));
		writeToc(dataFileBuffer, TocType.VALUES, metricCount, first
				.getOffsets().dataBlockOffset());

        for (PcpMetricInfo info : metricInfos) {
			dataFileBuffer.position(info.getOffsets().descriptorOffset());
			writeMetricsSection(dataFileBuffer, info.getMetricName(), info
					.getTypeHandler().getMetricType());

			dataFileBuffer.position(info.getOffsets().dataBlockOffset());
			writeValueSection(dataFileBuffer, info.getOffsets()
					.descriptorOffset(), info.getInitialValue(), info
					.getTypeHandler());
        }

        // Once it's set up, let the agent know
        dataFileBuffer.position(gen2Offset);
        dataFileBuffer.putLong(generation);
	}

    @Override
    protected int getFileLength(Collection<PcpMetricInfo> metricInfos) {
    	int count = metricInfos.size();
		return HEADER_LENGTH + (TOC_LENGTH * 2) + (METRIC_LENGTH * count)
				+ (VALUE_LENGTH * count);
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
        dataFileBuffer.putInt(firstEntryOffset);
    }

    /**
     * Writes the descriptor block for an individual metric to the file.
     * 
     * @param dataFileBuffer
     *            ByteBuffer positioned at the correct offset in the file for the block
     * @param name
     *            the name of the metric (must be &le; {@link #METRIC_NAME_LIMIT} characters, and
     *            must be convertible to {@link #PCP_CHARSET})
     * @param metricType
     *            the type of the metric
     */
    private void writeMetricsSection(ByteBuffer dataFileBuffer, String name,
            MmvMetricType metricType) {
        int originalPosition = dataFileBuffer.position();

        dataFileBuffer.put(name.getBytes(PCP_CHARSET));
        dataFileBuffer.put((byte) 0);
        dataFileBuffer.position(originalPosition + METRIC_NAME_LIMIT + 1);
        dataFileBuffer.putInt(metricType.getIdentifier());
        // Instance domains not yet supported
        dataFileBuffer.putInt(DEFAULT_INSTANCE_DOMAIN_ID);
        // Dimensions not yet supported
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
     */
    @SuppressWarnings("unchecked")
	private void writeValueSection(ByteBuffer dataFileBuffer,
			int descriptorOffset, Object value, TypeHandler<?> handler) {
        dataFileBuffer.putInt(descriptorOffset);
        // Instance offset
        dataFileBuffer.putInt(0);
        TypeHandler rawHandler = handler;
        rawHandler.putBytes(dataFileBuffer, value);
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
	protected synchronized PcpOffset getNextOffsets(int totalMetrics) {
		if (!firstOffsetCalculated) {
			calculateFirstOffset(totalMetrics);
		}
		PcpOffset offset = new PcpOffset(nextDescriptorOffset,
				nextDataBlockOffset, nextDataValueOffset);
		nextDataBlockOffset += VALUE_LENGTH;
		nextDataValueOffset += VALUE_LENGTH;
		nextDescriptorOffset += METRIC_LENGTH;
		return offset;
	}

	private void calculateFirstOffset(int totalMetrics) {
		nextDescriptorOffset = HEADER_LENGTH + (TOC_LENGTH * 2);
		nextDataBlockOffset = nextDescriptorOffset + (METRIC_LENGTH * totalMetrics);
		nextDataValueOffset = nextDataBlockOffset + DATA_VALUE_OFFSET_WITHIN_BLOCK;
		firstOffsetCalculated = true;
	}

    public static void main(String[] args) throws IOException {
        PcpMmvWriter bridge = new PcpMmvWriter(new File("/var/tmp/mmv/mmvtest2"));
        // Uses default boolean-to-int handler
        bridge.addMetric("sheep.baabaablack.bagsfull.haveany", new AtomicBoolean(true));
        // Uses default int handler
        bridge.addMetric("sheep.baabaablack.bagsfull.count", 3);
        // Uses default long handler
        bridge.addMetric("sheep.insomniac.count", 12345678901234L);
        // Uses default double handler
        bridge.addMetric("sheep.limpy.legs.available", 0.75);
        // addMetric(String) would fail, as there's no handler registered; use a custom one which
        // puts the string's length as an int
        bridge.addMetric("sheep.insomniac.jumpitem", "Fence", new AbstractTypeHandler<String>(
                MmvMetricType.I32) {
            public void putBytes(ByteBuffer buffer, String value) {
                buffer.putInt(value.length());
            }
        });
        // addMetric(Date) would fail, as there's no handler registered; register one for all date
        // types from now on
        bridge.registerType(Date.class, new AbstractTypeHandler<Date>(MmvMetricType.I64) {
            public void putBytes(ByteBuffer buffer, Date value) {
                buffer.putLong(value.getTime());
            }
        });
        bridge.addMetric("cow.how.now", new Date());
        bridge.addMetric("cow.how.then", new GregorianCalendar(1990, 1, 1, 12, 34, 56).getTime());
        bridge.start();
        // Sold a bag
        bridge.updateMetric("sheep.baabaablack.bagsfull.count", 2);
    }
}