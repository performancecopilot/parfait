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


import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.pcp.parfait.dxm.PcpMmvWriter.PCP_CHARSET;

final class PcpString implements PcpOffset,MmvWritable {

    static final int STRING_BLOCK_LENGTH = 256;
    static final int STRING_BLOCK_LIMIT = STRING_BLOCK_LENGTH - 1;

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

    @Override
    public void writeToMmv(ByteBuffer byteBuffer) {
        byteBuffer.position(offset);
        byte[] bytes = initialValue.getBytes(PCP_CHARSET);
        Preconditions.checkArgument(bytes.length < STRING_BLOCK_LENGTH);
        byteBuffer.put(bytes);
        byteBuffer.put((byte) 0);

    }

    static class PcpStringStore {
        private final Collection<PcpString> stringInfo = new CopyOnWriteArrayList<PcpString>();

        PcpString createPcpString(String text) {
            if (text == null) {
                return null;
            }
            PcpString string = new PcpString(text);
            stringInfo.add(string);
            return string;
        }

        Collection<PcpString> getStrings() {
            return stringInfo;
        }

    }

}