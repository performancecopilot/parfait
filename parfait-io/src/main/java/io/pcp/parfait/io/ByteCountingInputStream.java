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

package io.pcp.parfait.io;

import io.pcp.parfait.Counter;
import com.google.common.base.Preconditions;
import org.apache.commons.io.input.ProxyInputStream;

import java.io.IOException;
import java.io.InputStream;

public class ByteCountingInputStream extends ProxyInputStream {

	private final Counter byteCounter;

    public ByteCountingInputStream(InputStream streamToWrap,
            Counter counter) {
        super(streamToWrap);
        Preconditions.checkNotNull(counter, "MonitoredCounter cannot be null");
        Preconditions.checkNotNull(streamToWrap, "InputStream cannot be null");
        this.byteCounter = counter;
    }

    @Override
    public int read(byte[] bts, int st, int end) throws IOException {
        int read = super.read(bts, st, end);
        eosSafeCountingRead(read);
        return read;
    }

    @Override
    public int read(byte[] bts) throws IOException {
        int read = super.read(bts);
        eosSafeCountingRead(read);
        return read;
    }
    
    @Override
    public int read() throws IOException {
        int readByte = super.read();
        /*
         * the other read methods return the # bytes read, not the actual byte like this one does so
         * if we've reached EOS, we don't increment the counter, otherwise we do.
         */
        eosSafeCountingRead(readByte < 0 ? 0 : 1);
        return readByte;
    }

    private void eosSafeCountingRead(int numRead) {
        if (numRead > 0) {
            byteCounter.inc(numRead);
        }
    }
}
