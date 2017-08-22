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
import org.apache.commons.io.output.ProxyOutputStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Used to calculate the rate at which data is sent (downloaded) . When download outputstream
 * wrapped in a ByteCountingOutputStream it can update the counters as byte or byte[] chunks are sent ,
 * allowing us to truly see the actual rate of transfers happening.
 */

public class ByteCountingOutputStream extends ProxyOutputStream {

    private final Counter byteCounter;

    /**
     * Constructs a ByteCountingOutputStream
     * 
     * @param out
     *            OutputStream to be wrapped
     * @param counter
     *            PCP metric counter to be updated
     */

    public ByteCountingOutputStream(OutputStream out, Counter counter) {
        super(out);
        this.byteCounter = counter;
    }

    /** @see java.io.OutputStream#write(byte[]) */
    public void write(byte[] b) throws IOException {
        super.write(b);
        byteCounter.inc(b.length);
    }

    /** @see java.io.OutputStream#write(byte[], int, int) */
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        byteCounter.inc(len);
    }

    /** @see java.io.OutputStream#write(int) */
    public void write(int b) throws IOException {
        super.write(b);
        byteCounter.inc(4);// incr by 4 as int size is 4 bytes
    }

}
