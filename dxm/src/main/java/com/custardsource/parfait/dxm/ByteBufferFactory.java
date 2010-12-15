package com.custardsource.parfait.dxm;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ByteBufferFactory {
    ByteBuffer build(int length) throws IOException;
}
