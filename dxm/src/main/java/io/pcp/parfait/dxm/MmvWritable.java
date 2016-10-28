package io.pcp.parfait.dxm;

import java.nio.ByteBuffer;

public interface MmvWritable {

    void writeToMmv(ByteBuffer byteBuffer);

}