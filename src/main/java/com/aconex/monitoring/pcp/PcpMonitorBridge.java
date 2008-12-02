package com.aconex.monitoring.pcp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.Assert;

import com.aconex.monitoring.Monitor;
import com.aconex.monitoring.Monitorable;
import com.aconex.monitoring.MonitorableRegistry;

/**
 * PcpMonitorBridge bridges between the set of {@link Monitorable}s in the current system and a PCP
 * monitor agent. The bridge works by persisting any changes to a Monitorable into a section of
 * memory that is also mapped into the PCP monitor agents address space.
 * <p>
 * The format of the shared address space is specified in an associated header file. A full
 * description of the communication protocol can be found under the issue ACX-5426.
 * 
 * @author ohutchison
 */
@ManagedResource
public class PcpMonitorBridge implements Lifecycle {

    private final Logger LOG = Logger.getLogger(PcpMonitorBridge.class);

    public static final byte PROTOCOL_VERSION = 1;

    public static final int MAX_STRING_LENGTH = 256;

    public static final int UPDATE_QUEUE_SIZE = 1024;

    public static final String ENCODING = "ISO-8859-1";

    private final Map<Monitorable<?>, Integer> monitorableOffsets = new HashMap<Monitorable<?>, Integer>();

    private final ArrayBlockingQueue<Monitorable<?>> monitorablesPendingUpdate = new ArrayBlockingQueue<Monitorable<?>>(
    		UPDATE_QUEUE_SIZE);

    private final Monitor monitor = new PcpMonitorBridgeMonitor();

    private final Thread updateThread;

    private final String serverName;

    private final File dataFileDir;

    private final MonitorableRegistry registry;
    
    /*
     * Determines whether value changes detected are written out to an external file for external
     * monitoring by the Aconex PCP agent.
     */
    private boolean outputValuesToPCPFile = true;

    private volatile ByteBuffer dataFileBuffer = null;

    private CompositeType monitoredType;
    private String[] jmxMonitoredNames;
    private Object[] jmxMonitoredValues;
    private Map<String, Integer> jmxArrayIndexMap;

    private boolean deleteFilesOnExit = false;


    public PcpMonitorBridge(String serverName, String dataFileDir) {
    	this(serverName, dataFileDir, MonitorableRegistry.DEFAULT_REGISTRY);
    }

    public PcpMonitorBridge(String serverName, String dataFileDir, MonitorableRegistry registry) {
        Assert.hasText(serverName, "Sever name can not be blank");
        this.serverName = serverName;
        this.dataFileDir = new File(dataFileDir);
        if (!this.dataFileDir.exists()) {
            this.dataFileDir.mkdirs();
        }
        Assert.isTrue(this.dataFileDir.isDirectory(), "dataFileDir [" + dataFileDir
                + "] is not a directory.");
        this.updateThread = new Thread(new Updater());
        this.updateThread.setName("PcpMonitorBridge-Updater");
        this.updateThread.setDaemon(true);
        Assert.notNull(registry);
        this.registry = registry;
    }

    public boolean isRunning() {
        return dataFileBuffer != null;
    }

    public void start() {
        startMonitoring();
    }

    public void stop() {
        dataFileBuffer = null;
    }

    public boolean hasUpdatesPending() {
        return monitorablesPendingUpdate.size() > 0;
    }

    private void startMonitoring() {
        try {
            Collection<Monitorable<?>> monitorables = registry.getMonitorables();

            setupJmxValues(monitorables);

            // Calculate the data file offsets
            int dataFileOffset = 9; // 8 bytes for the version number and 1 for the protocol version
            for (Monitorable<?> monitorable : monitorables) {
                int dataSize = getTypeSize(monitorable.getType());
                dataFileOffset = align(dataFileOffset, dataSize);
                monitorableOffsets.put(monitorable, dataFileOffset);
                dataFileOffset = dataFileOffset + dataSize;
            }

            // Write the new header data into header file
            long fileGeneration = System.currentTimeMillis();
            File headerFile = getHeaderFile();
            if (isDeleteFilesOnExit()) {
                headerFile.deleteOnExit();
            }
            OutputStreamWriter headerWriter = new OutputStreamWriter(new BufferedOutputStream(
                    new FileOutputStream(headerFile)), ENCODING);
            writeHeaderValue(headerWriter, "version", String.valueOf(PROTOCOL_VERSION));
            writeHeaderValue(headerWriter, "generation", String.valueOf(fileGeneration));
            for (Monitorable<?> monitorable : monitorables) {
                writeHeaderValue(headerWriter, monitorable.getName(), monitorableOffsets
                        .get(monitorable)
                        + "," + getTypeName(monitorable.getType()));
            }
            headerWriter.close();

            // Create a new data file and populate with initial data
            createDataFile(dataFileOffset);
            for (Monitorable<?> monitorable : monitorables) {
                updateData(monitorable);
                monitorable.attachMonitor(monitor);
            }

            // And finally update the data file generation number to match the header file
            dataFileBuffer.put(8, PROTOCOL_VERSION);
            dataFileBuffer.putLong(0, fileGeneration);

            updateThread.start();

            LOG.info("PCP monitoring bridge started for server [" + serverName
                    + "] - logging to directory [" + dataFileDir + "]");
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialise PCP monitoring bridge", e);
        }
    }

    private void setupJmxValues(Collection<Monitorable<?>> monitorables) {
        try {
            jmxMonitoredNames = new String[monitorables.size()];
            String[] descriptions = new String[monitorables.size()];
            jmxMonitoredValues = new Object[monitorables.size()];
            OpenType<?>[] types = new OpenType<?>[monitorables.size()];
            jmxArrayIndexMap = new HashMap<String, Integer>(monitorables.size());
            int index = 0;

            for (Monitorable<?> monitorable : monitorables) {
                jmxMonitoredNames[index] = monitorable.getName();
                descriptions[index] = StringUtils.defaultIfEmpty(monitorable.getDescription(),
                        "(unknown)");
                types[index] = getJmxType(monitorable.getType());
                jmxArrayIndexMap.put(monitorable.getName(), index);
                index++;
            }

            monitoredType = new CompositeType("Exposed PCP metrics",
                    "Details of all exposed PCP metrics", jmxMonitoredNames, descriptions, types);
        } catch (OpenDataException e) {
            throw new UnsupportedOperationException("Unable to configure JMX types", e);
        }
    }

    private OpenType<?> getJmxType(Class<?> type) {
        if (type == Boolean.class) {
            return SimpleType.BOOLEAN;
        } else if (type == Integer.class || type == AtomicInteger.class) {
            return SimpleType.INTEGER;
        } else if (type == Long.class || type == AtomicLong.class) {
            return SimpleType.LONG;
        } else if (type == Double.class) {
            return SimpleType.DOUBLE;
        } else if (type == String.class) {
            return SimpleType.STRING;
        } else {
            throw new UnsupportedOperationException(
                    "Don't know how to process Monitorable of type [" + type + "]");
        }
    }

    @ManagedAttribute(description = "All exposed PCP metrics")
    public CompositeData getExposedMetrics() {
        try {
            return new CompositeDataSupport(monitoredType, jmxMonitoredNames, jmxMonitoredValues);
        } catch (OpenDataException e) {
            throw new RuntimeException(e);
        }
    }

    private int align(int offset, int dataSize) {
        int alignmentBoundry = dataSize == 8 ? 8 : 4;
        if (offset % alignmentBoundry != 0) {
            return ((offset / alignmentBoundry) + 1) * alignmentBoundry;
        } else {
            return offset;
        }
    }

    private void updateData(Monitorable<?> monitorable) {

        ByteBuffer localFileBuffer = dataFileBuffer;
        if (localFileBuffer == null) {
            return;
        }

        Class<?> type = monitorable.getType();
        localFileBuffer.position(monitorableOffsets.get(monitorable));
        Object jmxValue;

        if (type == Boolean.class) {
            int value = ((Boolean) monitorable.get()) ? 1 : 0;
            localFileBuffer.putInt(value);
            jmxValue = value;
        } else if (type == Integer.class) {
            int value = (Integer) monitorable.get();
            localFileBuffer.putInt((Integer) monitorable.get());
            jmxValue = value;
        } else if (type == AtomicInteger.class) {
            int value = ((AtomicInteger) monitorable.get()).intValue();
            localFileBuffer.putInt(value);
            jmxValue = value;
        } else if (type == Long.class) {
            long value = (Long) monitorable.get();
            localFileBuffer.putLong(value);
            jmxValue = value;
        } else if (type == AtomicLong.class) {
            long value = ((AtomicLong) monitorable.get()).longValue();
            localFileBuffer.putLong(value);
            jmxValue = value;
        } else if (type == Double.class) {
            double value = (Double) monitorable.get();
            localFileBuffer.putDouble(value);
            jmxValue = value;
        } else if (type == String.class) {
            try {
                String value = (String) monitorable.get();
                byte[] stringData = value.getBytes(ENCODING);
                int length = Math.min(stringData.length, MAX_STRING_LENGTH - 1);
                localFileBuffer.put(stringData, 0, length);
                localFileBuffer.put((byte) 0);
                jmxValue = value;
            } catch (UnsupportedEncodingException e) {
                throw new UnsupportedOperationException(e);
            }
        } else {
            throw new UnsupportedOperationException(
                    "Don't know how to process Monitorable of type [" + type + "]");
        }

        jmxMonitoredValues[jmxArrayIndexMap.get(monitorable.getName())] = jmxValue;
    }

    private int getTypeSize(Class<?> type) {
        if (type == Boolean.class) {
            return 4;
        } else if (type == Integer.class || type == AtomicInteger.class) {
            return 4;
        } else if (type == Long.class || type == AtomicLong.class) {
            return 8;
        } else if (type == Double.class) {
            return 8;
        } else if (type == String.class) {
            return MAX_STRING_LENGTH;
        } else {
            throw new UnsupportedOperationException(
                    "Don't know how to process Monitorable of type [" + type + "]");
        }
    }

    private String getTypeName(Class<?> type) {
        if (type == Boolean.class) {
            return "int";
        } else if (type == Integer.class || type == AtomicInteger.class) {
            return "int";
        } else if (type == Long.class || type == AtomicLong.class) {
            return "long";
        } else if (type == Double.class) {
            return "double";
        } else if (type == String.class) {
            return "string";
        } else {
            throw new UnsupportedOperationException(
                    "Don't know how to process Monitorable of type [" + type + "]");
        }
    }

    private File getHeaderFile() {
        return new File(dataFileDir, serverName + ".pcp.header");
    }

    private void writeHeaderValue(Writer output, String name, String value) throws IOException {
        output.append(name);
        output.append('=');
        output.append(value);
        output.append("\n");
    }

    private File getDataFile() {
        return new File(dataFileDir, serverName + ".pcp.data");
    }

    /**
     * Creates a new blank data file of the specified length, all bytes are initially cleared to
     * zero.
     */
    private void createDataFile(int totalLength) throws IOException {
        RandomAccessFile fos = null;
        try {
            File dataFile = getDataFile();
            if (isDeleteFilesOnExit()) {
                dataFile.deleteOnExit();
            }
            fos = new RandomAccessFile(dataFile, "rw");
            fos.setLength(0);
            fos.setLength(totalLength);
            ByteBuffer tempDataFile = fos.getChannel().map(MapMode.READ_WRITE, 0, totalLength);
            tempDataFile.order(ByteOrder.nativeOrder());
            fos.close();

            dataFileBuffer = tempDataFile;
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * The Updater is responsible for taking any Monitorables that are pending in the update queue
     * and saving their current value to the PCP shared data file.
     */
    private class Updater implements Runnable {

        public void run() {
            try {
                Collection<Monitorable<?>> monitorablesToUpdate = new ArrayList<Monitorable<?>>();
                while (dataFileBuffer != null) {
                    try {
                        monitorablesToUpdate.add(monitorablesPendingUpdate.take());
                        monitorablesPendingUpdate.drainTo(monitorablesToUpdate);
                        for (Monitorable<?> monitorable : monitorablesToUpdate) {
                            updateData(monitorable);
                        }
                        if (monitorablesPendingUpdate.size() >= UPDATE_QUEUE_SIZE) {
                            LOG.warn("Update queue was full - some updates may have been lost.");
                        }
                        monitorablesToUpdate.clear();
                    } catch (InterruptedException e) {
                        LOG.error("Updater was unexpectedly interrupted", e);
                    }
                }
            } catch (RuntimeException e) {
                LOG.fatal("Updater dying because of unexpected exception", e);
                throw e;
            } catch (Error e) {
                LOG.fatal("Updater dying because of unexpected exception", e);
                throw e;
            }
        }
    }

    /**
     * Responsible for adding any Monitorables that change to the queue of Monitorables that are
     * pending update. This class will never block, if the update queue is ever full then the we
     * just do nothing.
     */
    private class PcpMonitorBridgeMonitor implements Monitor {

        public void valueChanged(Monitorable<?> monitorable) {
            /*
             * If the master-arm switch to output values to a file is off, then abandon quickly. The
             * only reason it would be turned off is because we have suspected it is causing
             * performance grief. Highly unlikely, but just in case.
             */
            if (!isOutputValuesToPCPFile()) {
                return;
            }

            if (!monitorablesPendingUpdate.offer(monitorable)) {
                // The queue must be full... This will get detected by the Updater and logged we
                // should do nothing here as we don't want to block.
            }
        }
    }

    @ManagedAttribute(description = "If set, value changes are written to an external file monitored by the Aconex PCP Agent.")
    public boolean isOutputValuesToPCPFile() {
        return outputValuesToPCPFile;
    }

    @ManagedAttribute
    public void setOutputValuesToPCPFile(boolean outputValuesToPCPFile) {
        this.outputValuesToPCPFile = outputValuesToPCPFile;
    }

    /**
     * When set to true, both the header and data files created by this bridge are marked for deletion 
     * via JVM standard deletion policy on normal termination.  The default value is false, and highly encouraged
     * to leave this value off.  Only when circumstances require that the data file name pattern changes on each 
     * JVM launch (say, when using the Process ID in the filename) should this properties use be considered.
     * @param deleteFilesOnExit
     */
    public void setDeleteFilesOnExit(boolean deleteFilesOnExit) {
        this.deleteFilesOnExit = deleteFilesOnExit;
    }

    /**
     * 
     * @return
     */
    public final boolean isDeleteFilesOnExit() {
        return deleteFilesOnExit;
    }
}
