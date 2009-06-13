package com.custardsource.parfait.pcp;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
public class PcpMmvWriter {
    private static enum TocType {
        METRICS(2),
        VALUES(3);

        private final int identifier;

        private TocType(int identifier) {
            this.identifier = identifier;
        }
    }

    private static class PcpMetricInfo {
        public PcpMetricInfo(int valueIndex, TypeHandler<?> handler, Object initialValue) {
            this.valueIndex = valueIndex;
            this.typeHandler = handler;
            this.initialValue = initialValue;
        }

        private final int valueIndex;
        private final Object initialValue;
        private final TypeHandler<?> typeHandler;
    }

    /**
     * The charset used for PCP metrics names and String values.
     */
    public static final Charset PCP_CHARSET = Charset.forName("US-ASCII");
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
    private static final byte[] TAG = "MMV\0".getBytes(PCP_CHARSET);
    private static final int MMV_FORMAT_VERSION = 0;

    private final File dataFile;
    private ByteBuffer dataFileBuffer = null;
    private volatile boolean started = false;
    private final Map<String, PcpMetricInfo> metricData = new LinkedHashMap<String, PcpMetricInfo>();
    private final Map<Class<?>, TypeHandler<?>> typeHandlers = new HashMap<Class<?>, TypeHandler<?>>(
            DefaultTypeHandlers.getDefaultMappings());
    private int metricCount = 0;

    /**
     * Creates a new PcpMmvFile writing to the underlying file, which will be created + opened as a
     * memory-mapped file. Uses the provided architecture to determine whether to write 32- or
     * 64-bit longs for some key header fields.
     * 
     * @param file
     *            the file to map
     */
    public PcpMmvWriter(File file) {
        this.dataFile = file;
    }

    /**
     * Adds a new metric to the writer, with an initial default value. Uses the default
     * {@link TypeHandler} based on the runtime type of the initialValue parameter.
     * 
     * @param name
     *            the name of the metric to export. Must not exceed {@link #METRIC_NAME_LIMIT} bytes
     *            when converted using {@link #PCP_CHARSET}
     * @param initialValue
     *            the 'default' value to write into the file at initialisation time
     * @throws IllegalArgumentException
     *             if the name is too long, the metric name has already been added, or this is no
     *             type handler registered for the runtime class of the initial value
     * @throws IllegalStateException
     *             if this writer has already been started, finalising the file layout
     */
    public void addMetric(String name, Object initialValue) {
        TypeHandler<?> handler = typeHandlers.get(initialValue.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No default handler registered for type "
                    + initialValue.getClass());
        }
        addMetricInfo(name, initialValue, handler);

    }

    /**
     * Adds a new metric to the writer, with an initial default value. Uses the default
     * {@link TypeHandler} based on the runtime type of the initialValue parameter.
     * 
     * @param name
     *            the name of the metric to export. Must not exceed {@link #METRIC_NAME_LIMIT} bytes
     *            when converted using {@link #PCP_CHARSET}
     * @param initialValue
     *            the 'default' value to write into the file at initialisation time
     * @param pcpType
     *            the type converter to use to render the initial value (and all subsequent values)
     *            to the PCP stream
     * @throws IllegalArgumentException
     *             if the name is too long or the metric name has already been added
     * @throws IllegalStateException
     *             if this writer has already been started, finalising the file layout
     */
    public <T> void addMetric(String name, T initialValue, TypeHandler<T> pcpType) {
        if (pcpType == null) {
            throw new IllegalArgumentException("PCP Type handler must not be null");
        }
        addMetricInfo(name, initialValue, pcpType);
    }

    /**
     * Updates the metric value of the given metric, once the writer has been started
     * 
     * @param name
     *            the metric to update
     * @param value
     *            the new value (must be convertible by the {@link TypeHandler} used when adding the
     *            metric)
     */
    public void updateMetric(String name, Object value) {
        if (!started) {
            throw new IllegalStateException("Cannot update metric unless writer is running");
        }
        PcpMetricInfo info = metricData.get(name);
        if (info == null) {
            throw new IllegalArgumentException("Metric " + name
                    + " was not added before initialising the writer");
        }
        dataFileBuffer.position(getValueOffset(info.valueIndex, metricCount));
        writeValueSection(dataFileBuffer, info.valueIndex, value, info.typeHandler);

    }

    /**
     * Registers a new {@link TypeHandler} to be used to convert all subsequent values of type
     * runtimeClass
     * 
     * @param runtimeClass
     *            the class to be converted by the new handler
     * @param handler
     *            the handler to use
     */
    public <T> void registerType(Class<T> runtimeClass, TypeHandler<T> handler) {
        if (started) {
            // Can't add any more metrics anyway; harmless
            return;
        }
        typeHandlers.put(runtimeClass, handler);
    }

    /**
     * Starts the Writer, freezing the file format and writing out the metadata and initial values.
     * 
     * @throws IOException
     *             if the file cannot be created or written.
     */
    public void start() throws IOException {
        if (started) {
            throw new IllegalStateException("Writer is already started");
        }
        if (metricCount == 0) {
            throw new IllegalStateException("Cannot create an MMV file with no metrics");
        }
        dataFileBuffer = initialiseBuffer();

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

        dataFileBuffer.position(getTocOffset(0));
        writeToc(dataFileBuffer, TocType.METRICS, metricCount, getMetricOffset(0));
        dataFileBuffer.position(getTocOffset(1));
        writeToc(dataFileBuffer, TocType.VALUES, metricCount, getValueOffset(0, metricCount));

        for (Map.Entry<String, PcpMetricInfo> metricEntry : metricData.entrySet()) {
            PcpMetricInfo info = metricEntry.getValue();

            dataFileBuffer.position(getMetricOffset(info.valueIndex));
            writeMetricsSection(dataFileBuffer, metricEntry.getKey(), info.typeHandler
                    .getMetricType());

            dataFileBuffer.position(getValueOffset(info.valueIndex, metricCount));
            writeValueSection(dataFileBuffer, info.valueIndex, info.initialValue, info.typeHandler);
        }

        // Once it's set up, let the agent know
        dataFileBuffer.position(gen2Offset);
        dataFileBuffer.putLong(generation);

        started = true;
    }

    private ByteBuffer initialiseBuffer() throws IOException {
        RandomAccessFile fos = null;
        try {
            fos = new RandomAccessFile(dataFile, "rw");
            fos.setLength(0);
            int length = getValueOffset(metricCount - 1, metricCount) + METRIC_LENGTH;
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

    private void addMetricInfo(String name, Object initialValue, TypeHandler<?> pcpType) {
        if (started) {
            throw new IllegalStateException("Cannot add metric " + name + " after starting");
        }
        if (metricData.containsKey(name)) {
            throw new IllegalArgumentException("Metric " + name
                    + " has already been added to writer");
        }
        if (name.getBytes(PCP_CHARSET).length > METRIC_NAME_LIMIT) {
            throw new IllegalArgumentException("Cannot add metric " + name
                    + "; name exceeds length limit");
        }
        PcpMetricInfo info = new PcpMetricInfo(metricCount++, pcpType, initialValue);
        metricData.put(name, info);
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
     * @param metricIndex
     *            the 0-based index of the metric this value represents
     * @param value
     *            the value to be written to the file
     * @param handler
     *            the {@link TypeHandler} to use to convert the value to bytes
     */
    @SuppressWarnings("unchecked")
    private void writeValueSection(ByteBuffer dataFileBuffer, int metricIndex, Object value,
            TypeHandler<?> handler) {
        dataFileBuffer.putInt(getMetricOffset(metricIndex));
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

    /**
     * Calculates the file offset of a given PCP MMV metric descriptor block
     * 
     * @param metricIndex
     *            the 0-based index of the metric block to be written
     * @return the file offset used to store that metric block (32-bit regardless of architecture)
     */
    private final int getMetricOffset(int metricIndex) {
        return HEADER_LENGTH + (TOC_LENGTH * 2) + (metricIndex * METRIC_LENGTH);
    }

    /**
     * Calculates the file offset of a given PCP MMV metric value block
     * 
     * @param metricIndex
     *            the 0-based index of the metric value to be written
     * @param totalMetrics
     *            the total number of metrics represented in the file (used to leave room for all
     *            metric descriptor blocks)
     * @return the file offset used to store that value block (32-bit regardless of architecture)
     */
    private final int getValueOffset(int metricIndex, int totalMetrics) {
        return HEADER_LENGTH + (TOC_LENGTH * 2) + (totalMetrics * METRIC_LENGTH)
                + (metricIndex * VALUE_LENGTH);
    }
    
    public static void main(String[] args) throws IOException {
        PcpMmvWriter bridge = new PcpMmvWriter(new File("/var/tmp/mmv/mmvtest"));
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