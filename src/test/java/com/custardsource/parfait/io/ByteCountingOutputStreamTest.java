package com.aconex.monitoring.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.aconex.monitoring.MonitorableRegistry;
import com.aconex.monitoring.MonitoredCounter;
import com.aconex.monitoring.io.ByteCountingOutputStream;

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
