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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.pcp.parfait.MonitorableRegistry;
import io.pcp.parfait.MonitoredCounter;
import io.pcp.parfait.io.ByteCountingOutputStream;

import junit.framework.TestCase;

public class ByteCountingOutputStreamTest extends TestCase {

    private MonitoredCounter counter = null;
    private ByteArrayOutputStream baos;
    private ByteCountingOutputStream bcos;
    private MonitorableRegistry registry = new MonitorableRegistry();

    protected void setUp() throws Exception {
        super.setUp();
        counter = new MonitoredCounter("food", "", registry);
        this.baos = new ByteArrayOutputStream();
        this.bcos = new ByteCountingOutputStream(this.baos, counter);
    }

	protected void tearDown() throws Exception {
        this.baos = null;
        this.bcos = null;
        super.tearDown();
    }

    public void testWriteInt() throws IOException {
        assertEquals(0, counter.get().longValue());
        this.bcos.write(5);// write an integer ( 4 bytes)
        assertEquals(4, counter.get().longValue());

    }

    public void testWriteByteArray() throws IOException {
        assertEquals(0, counter.get().longValue());
        String ex = "Nothing is impossible in this world except rizwan winning a lottery";
        byte[] buf = ex.getBytes();
        this.bcos.write(buf);
        assertEquals(buf.length, counter.get().longValue());
    }

    public void testWriteByteArrayIntInt() throws IOException {
        assertEquals(0, counter.get().longValue());
        String ex = "Nothing is impossible in this world except rizwan winning a lottery";
        byte[] buf = ex.getBytes();
        this.bcos.write(buf, 0, 10);
        assertEquals(10, counter.get().longValue());
    }

}
