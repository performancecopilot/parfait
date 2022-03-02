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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import io.pcp.parfait.MonitorableRegistry;
import io.pcp.parfait.MonitoredCounter;

public class ByteCountingInputStreamTest extends TestCase {
    private static final byte[] TEST_BYTES = new byte[] { 0, 1, 2, 3, 4 };
    private MonitoredCounter counter;
    private ByteCountingInputStream bcis;
    private final MonitorableRegistry registry = new MonitorableRegistry();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        counter = new MonitoredCounter("food", "", registry);
        ByteArrayInputStream bais = new ByteArrayInputStream(TEST_BYTES);
        this.bcis = new ByteCountingInputStream(bais, counter);
    }

    public void testBytesRead() throws IOException {
        assertEquals(0, counter.get().longValue());

        for (int i = 0; i < TEST_BYTES.length; i++) {
            assertEquals(TEST_BYTES[i], bcis.read());
            assertEquals(i + 1, counter.get().longValue());
        }
        long lastCounterValue = counter.get();
        
        assertEquals("Should have reached End Of Stream", -1, bcis.read());
        assertEquals("Counter should not have changed when attempt to read past EOS",
                lastCounterValue, counter.get().longValue());

    }
    
    

    public void testByteBlockRead() throws IOException {
        assertEquals(0, counter.get().longValue());

        byte[] testByteRange = new byte[TEST_BYTES.length];
        bcis.read(testByteRange);
        assertEquals("Should have counted all the reads", TEST_BYTES.length, counter.get()
                .longValue());
    }
    
    public void testByteBlockReadSmaller() throws IOException {
        assertEquals(0, counter.get().longValue());

        byte[] testByteRange = new byte[TEST_BYTES.length - 1];
        bcis.read(testByteRange);
        assertEquals("Should have counted all the reads", testByteRange.length, counter.get()
                .longValue());

    }

    public void testByteBlockReadSizeMismatch() throws IOException {
        assertEquals(0, counter.get().longValue());

        /*
         * Allocate larger block than the size we'll actually read to confirm it counts the right
         * number of _actual_ bytes read.
         */
        byte[] testByteRange = new byte[TEST_BYTES.length + 10];
        bcis.read(testByteRange);
        assertEquals("Should have counted all the reads", TEST_BYTES.length, counter.get()
                .longValue());
    }

    public void testByteBlockRangeRead() throws IOException {
        assertEquals(0, counter.get().longValue());

        byte[] testByteRange = new byte[TEST_BYTES.length];
        bcis.read(testByteRange, 0, TEST_BYTES.length);
        assertEquals("Should have counted all the reads", TEST_BYTES.length, counter.get()
                .longValue());
    }
    

    public void testByteBlockReadSizeSmaller() throws IOException {
        assertEquals(0, counter.get().longValue());

        byte[] testByteRange = new byte[TEST_BYTES.length - 2];
        bcis.read(testByteRange);
        assertEquals("Should have counted all the reads", testByteRange.length, counter.get()
                .longValue());
    }

    public void testByteBlockRangeReadSizeMismatch() throws IOException {
        assertEquals(0, counter.get().longValue());

        byte[] testByteRange = new byte[TEST_BYTES.length + 10];
        bcis.read(testByteRange, 0, TEST_BYTES.length);
        assertEquals("Should have counted all the reads", TEST_BYTES.length, counter.get()
                .longValue());
    }

}
