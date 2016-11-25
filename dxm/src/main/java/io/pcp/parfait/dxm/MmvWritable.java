package io.pcp.parfait.dxm;

import java.nio.ByteBuffer;

interface MmvWritable {

    int getOffset();
    void writeToMmv(ByteBuffer byteBuffer);

}