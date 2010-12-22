package com.custardsource.parfait.dxm;

import java.io.IOException;
import java.nio.ByteBuffer;

interface ByteBufferFactory {
    ByteBuffer build(int length) throws IOException;
}
