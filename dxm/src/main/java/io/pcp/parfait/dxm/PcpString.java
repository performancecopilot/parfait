/**
 * 
 */
package io.pcp.parfait.dxm;


import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;

import static io.pcp.parfait.dxm.PcpMmvWriter.PCP_CHARSET;

final class PcpString implements PcpOffset,MmvWritable {

    static final int STRING_BLOCK_LENGTH = 256;

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

    @Override
    public int byteSize() {
        return STRING_BLOCK_LENGTH;
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