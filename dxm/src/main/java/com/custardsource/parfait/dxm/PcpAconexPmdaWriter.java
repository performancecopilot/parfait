package com.custardsource.parfait.dxm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;

import com.custardsource.parfait.dxm.types.AbstractTypeHandler;
import com.custardsource.parfait.dxm.types.MmvMetricType;
import com.google.common.base.Preconditions;

public class PcpAconexPmdaWriter extends BasePcpWriter {
	public static final String ENCODING = "ISO-8859-1";
    public static final Charset CHARSET = Charset.forName(ENCODING);
    public static final byte PROTOCOL_VERSION = 1;
    public static final int METRIC_NAME_LIMIT = 200;
    public static final int MAX_STRING_LENGTH = 256;
	private static final int HEADER_LENGTH = 9; // 8 for version, 1 for protocol version
	private final File dataFileDirectory;
	private final File headerFile;
	private final String serverName;
	

	public PcpAconexPmdaWriter(File directory, String serverName, boolean deleteFilesOnExit) {
		super(getDataFile(directory, serverName), IdentifierSourceSet.DEFAULT_SET);
		Preconditions.checkArgument(serverName != null
				&& !serverName.equals(""), "Sever name can not be blank");
		this.serverName = serverName;
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new RuntimeException("Count not create output directory " + directory);
			}
		}
		Preconditions.checkArgument(directory.isDirectory(),
				"dataFileDir [%s] is not a directory.", directory);

		this.headerFile = getHeaderFile(directory, serverName);
		this.dataFileDirectory = directory; 
		
		if (deleteFilesOnExit) {
			getDataFile().deleteOnExit();
			headerFile.deleteOnExit();
		}
		registerType(String.class, new FixedLengthStringHandler(
				MmvMetricType.STRING, MAX_STRING_LENGTH));
	}

    private File getHeaderFile(File directory, String serverName) {
    	return new File(directory, serverName + ".pcp.header");
	}

	private static File getDataFile(File directory, String serverName) {
    	return new File(directory, serverName + ".pcp.data");
	}

	private void writeHeaderValue(Writer output, String name, String value) throws IOException {
        output.append(name);
        output.append('=');
        output.append(value);
        output.append("\n");
    }

	@Override
	protected Charset getCharset() {
		return CHARSET;
	}

	@Override
	protected int getFileLength() {
		PcpValueInfo lastInfo = null;
		for (PcpValueInfo info : getValueInfos()) {
			lastInfo = info;
		}
        return lastInfo.getOffset() + lastInfo.getTypeHandler().getDataLength();
	}

    private int align(int offset, int dataSize) {
        int alignmentBoundry = dataSize == 8 ? 8 : 4;
        if (offset % alignmentBoundry != 0) {
            return ((offset / alignmentBoundry) + 1) * alignmentBoundry;
        } else {
            return offset;
        }
    }

    @Override
    protected int getMetricNameLimit() {
        return METRIC_NAME_LIMIT;
    }

    @Override
    protected int getInstanceNameLimit() {
        return 0;
    }

	@Override
	protected synchronized void initialiseOffsets() {
		// Instance + Descriptor offsets not used by this file format; block + value
		// offsets are the same as there's no metadata
	    int nextOffset = HEADER_LENGTH;

        for (PcpValueInfo value : getValueInfos()) {
            nextOffset = align(nextOffset, value.getTypeHandler().getDataLength());
            value.setOffset(nextOffset);
            nextOffset += value.getTypeHandler().getDataLength();
        }
	}

	@Override
	protected void populateDataBuffer(ByteBuffer dataFileBuffer,
			Collection<PcpValueInfo> metricInfos) throws IOException {
		long fileGeneration = System.currentTimeMillis();
		// Need to write header here too
		OutputStreamWriter headerWriter = new OutputStreamWriter(
				new BufferedOutputStream(new FileOutputStream(headerFile)),
				ENCODING);
		writeHeaderValue(headerWriter, "version", String
				.valueOf(PROTOCOL_VERSION));
		writeHeaderValue(headerWriter, "generation", String
				.valueOf(fileGeneration));
		for (PcpValueInfo metricInfo : metricInfos) {
            writeHeaderValue(headerWriter, metricInfo.getMetricName().getMetric(), metricInfo
                    .getOffset()
                    + "," + metricInfo.getTypeHandler().getMetricType().getDescription());
		}
        headerWriter.close();
        
		for (PcpValueInfo metricInfo : metricInfos) {
			updateValue(metricInfo, metricInfo.getInitialValue());
		}
        
        // And finally update the data file generation number to match the header file
        dataFileBuffer.put(8, PROTOCOL_VERSION);
        dataFileBuffer.putLong(0, fileGeneration);

        
	}

	@Override
	public String toString() {
		return "PcpAconexPmdaWriter '" + serverName + "', writing to " + dataFileDirectory;
	}

	private static final class FixedLengthStringHandler extends
			AbstractTypeHandler<String> {
		private FixedLengthStringHandler(MmvMetricType type, int dataLength) {
			super(type, dataLength);
		}

		public void putBytes(ByteBuffer buffer, String value) {
			byte[] stringData = value.getBytes(CHARSET);
			int length = Math.min(stringData.length, MAX_STRING_LENGTH - 1);
			buffer.put(stringData, 0, length);
			buffer.put((byte) 0);
		}
	}

}
