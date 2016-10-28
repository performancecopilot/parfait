/**
 * 
 */
package io.pcp.parfait.dxm;


import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;

import static io.pcp.parfait.dxm.PcpMmvWriter.PCP_CHARSET;
import static io.pcp.parfait.dxm.PcpMmvWriter.STRING_BLOCK_LENGTH;

final class PcpString implements PcpOffset,MmvWritable {
    private final String initialValue;
    private int offset;
    
    public PcpString(String value) {
        this.initialValue = value;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    String getInitialValue() {
        return initialValue;
    }

    @Override
    public void writeToMmv(ByteBuffer byteBuffer) {
        byteBuffer.position(offset);
        byte[] bytes = initialValue.getBytes(PCP_CHARSET);
        Preconditions.checkArgument(bytes.length < STRING_BLOCK_LENGTH);
        byteBuffer.put(bytes);
        byteBuffer.put((byte) 0);

    }

}