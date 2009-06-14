package com.custardsource.parfait.pcp;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.custardsource.parfait.pcp.types.DefaultTypeHandlers;
import com.custardsource.parfait.pcp.types.TypeHandler;

public abstract class BasePcpWriter implements PcpWriter {
	private final File dataFile;
    private final Map<String, PcpMetricInfo> metricData = new LinkedHashMap<String, PcpMetricInfo>();
    private final Map<Class<?>, TypeHandler<?>> typeHandlers = new HashMap<Class<?>, TypeHandler<?>>(
            DefaultTypeHandlers.getDefaultMappings());
    protected volatile boolean started = false;
    private ByteBuffer dataFileBuffer = null;

    protected BasePcpWriter(File dataFile) {
    	this.dataFile = dataFile;
    }
    
    /* (non-Javadoc)
	 * @see com.custardsource.parfait.pcp.PcpWriter#addMetric(java.lang.String, java.lang.Object)
	 */
    public void addMetric(String name, Object initialValue) {
        TypeHandler<?> handler = typeHandlers.get(initialValue.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No default handler registered for type "
                    + initialValue.getClass());
        }
        addMetricInfo(name, initialValue, handler);

    }

    /* (non-Javadoc)
	 * @see com.custardsource.parfait.pcp.PcpWriter#addMetric(java.lang.String, T, com.custardsource.parfait.pcp.types.TypeHandler)
	 */
    public <T> void addMetric(String name, T initialValue, TypeHandler<T> pcpType) {
        if (pcpType == null) {
            throw new IllegalArgumentException("PCP Type handler must not be null");
        }
        addMetricInfo(name, initialValue, pcpType);
    }

    /* (non-Javadoc)
	 * @see com.custardsource.parfait.pcp.PcpWriter#registerType(java.lang.Class, com.custardsource.parfait.pcp.types.TypeHandler)
	 */
    public <T> void registerType(Class<T> runtimeClass, TypeHandler<T> handler) {
        if (started) {
            // Can't add any more metrics anyway; harmless
            return;
        }
        typeHandlers.put(runtimeClass, handler);
    }

    /* (non-Javadoc)
	 * @see com.custardsource.parfait.pcp.PcpWriter#updateMetric(java.lang.String, java.lang.Object)
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
        updateValue(info, value);
    }

    
	@SuppressWarnings("unchecked")
	protected void updateValue(PcpMetricInfo info, Object value) {
		dataFileBuffer.position(info.getOffsets().dataValueOffset());
        TypeHandler rawHandler = info.getTypeHandler();
        rawHandler.putBytes(dataFileBuffer, value);
	}

	private void addMetricInfo(String name, Object initialValue, TypeHandler<?> pcpType) {
        if (started) {
            throw new IllegalStateException("Cannot add metric " + name + " after starting");
        }
        if (metricData.containsKey(name)) {
            throw new IllegalArgumentException("Metric " + name
                    + " has already been added to writer");
        }
        if (name.getBytes(getCharset()).length > getMetricNameLimit()) {
            throw new IllegalArgumentException("Cannot add metric " + name
                    + "; name exceeds length limit");
        }
		PcpMetricInfo info = new PcpMetricInfo(name, pcpType,
				initialValue);
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

    /* (non-Javadoc)
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
		dataFileBuffer = initialiseBuffer(dataFile, getFileLength(metricData.values()));
        populateDataBuffer(dataFileBuffer, metricData.values());

        started = true;
    }
    
    private void initialiseOffsets() {
    	int totalMetrics = metricData.size();
    	for (PcpMetricInfo info : metricData.values()) {
    		info.setOffsets(getNextOffsets(info, totalMetrics));
    	}
	}

	protected abstract void populateDataBuffer(ByteBuffer dataFileBuffer,
			Collection<PcpMetricInfo> metricInfos) throws IOException;

	protected abstract PcpOffset getNextOffsets(PcpMetricInfo currentInfo,
			int totalMetrics);

    protected abstract int getMetricNameLimit();
    protected abstract Charset getCharset();
    protected abstract int getFileLength(Collection<PcpMetricInfo> infos);

	protected static class PcpMetricInfo {
        public PcpMetricInfo(String metricName, TypeHandler<?> handler, Object initialValue) {
        	this.metricName = metricName;
            this.typeHandler = handler;
            this.initialValue = initialValue;
        }

        private final String metricName;
		private final Object initialValue;
        private final TypeHandler<?> typeHandler;
        private PcpOffset offsets;
        
        public String getMetricName() {
        	return metricName;
        }
        
        public PcpOffset getOffsets() {
			return offsets;
		}
        
        public void setOffsets(PcpOffset offsets) {
        	this.offsets = offsets;
        }

		public TypeHandler<?> getTypeHandler() {
			return typeHandler;
		}

		public Object getInitialValue() {
        	return initialValue;
        }

    }

	protected static class PcpOffset {
		private final int descriptorOffset;
		private final int dataBlockOffset;
		private final int dataValueOffset;

		public PcpOffset(int descriptorOffset, int dataBlockOffset, int dataOffset) {
			this.descriptorOffset = descriptorOffset;
			this.dataBlockOffset = dataBlockOffset;
			this.dataValueOffset = dataOffset;
		}

		public int dataValueOffset() {
			return dataValueOffset;
		}

		public int descriptorOffset() {
			return descriptorOffset;
		}

		public int dataBlockOffset() {
			return dataBlockOffset;
		}
		
	}
}
