package com.custardsource.parfait.dxm;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Builds a ByteBuffer in memory, mostly useful for unit testing purposes
 */
public class InMemoryByteBufferFactory implements ByteBufferFactory{
    private ByteBuffer byteBuffer;
    private int numAllocations;

    @Override
    public ByteBuffer build(int length) throws IOException {
        byteBuffer =  ByteBuffer.allocate(length);
        numAllocations++;
        return byteBuffer;
    }

    /**
     * Returns the <em>last</em> allocated ByteBuffer used during creation, so that further inspection can be done during unit tests.
     *
     * You are reminded that this class may be used multiple times, this reference is only the last one created
     */
    public ByteBuffer getAllocatedBuffer() {
        return byteBuffer;
    }

    /**
     * Returns the # ByteBuffer allocations that have occured, since this Factory may produce more than one in its
     * life
     */
    public int getNumAllocations() {
        return numAllocations;
    }
}
