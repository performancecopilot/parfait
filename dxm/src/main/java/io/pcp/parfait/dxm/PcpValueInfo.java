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

/**
 * 
 */
package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpString.PcpStringStore;
import io.pcp.parfait.dxm.types.TypeHandler;

import java.nio.ByteBuffer;

import static io.pcp.parfait.dxm.PcpMmvWriter.DATA_VALUE_LENGTH;
import static io.pcp.parfait.dxm.PcpString.STRING_BLOCK_LIMIT;

public final class PcpValueInfo implements PcpOffset,MmvWritable {

    private static final int VALUE_LENGTH = 32;

	private final MetricName metricName;
	private final Object initialValue;
	private final PcpMetricInfo metricInfo;
	private final Instance instance;
	private final PcpString largeValue;
	private int offset;

    PcpValueInfo(MetricName metricName, PcpMetricInfo metricInfo, Instance instance, 
    		Object initialValue, PcpStringStore stringStore) {
        this.metricName = metricName;
        this.metricInfo = metricInfo;
        this.instance = instance;
        this.initialValue = initialValue;
        if (metricInfo.getTypeHandler().requiresLargeStorage()) {
            this.largeValue = stringStore.createPcpString(initialValue.toString());
        } else {
            this.largeValue = null;
        }
    }

    public MetricName getMetricName() {
        return metricName;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public int byteSize() {
        return VALUE_LENGTH;
    }

    public TypeHandler<?> getTypeHandler() {
        return metricInfo.getTypeHandler();
    }

    private Object getInitialValue() {
        return initialValue;
    }

    private int getInstanceOffset() {
        return instance == null ? 0 : instance.getOffset();
    }

    private int getDescriptorOffset() {
        return metricInfo.getOffset();
    }
    
    PcpString getLargeValue() {
        return largeValue;
    }

    @Override
    public void writeToMmv(ByteBuffer byteBuffer) {
        byteBuffer.position(offset);
        writeValueSection(byteBuffer);

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void writeValueSection(ByteBuffer dataFileBuffer) {
        int originalPosition = dataFileBuffer.position();
        TypeHandler rawHandler = getTypeHandler();
        if (rawHandler.requiresLargeStorage()) {
            // API requires the length here but it's currently unused -- write out the maximum
            // possible length
            dataFileBuffer.putLong(STRING_BLOCK_LIMIT);
            dataFileBuffer.putLong(getLargeValue().getOffset());
            dataFileBuffer.position(getLargeValue().getOffset());
        }
        rawHandler.putBytes(dataFileBuffer, getInitialValue());
        dataFileBuffer.position(originalPosition + DATA_VALUE_LENGTH);
        dataFileBuffer.putLong(getDescriptorOffset());
        dataFileBuffer.putLong(getInstanceOffset());
    }


}