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
    private ByteCountingOutputStream pcpCS;

    protected void setUp() throws Exception {
        super.setUp();
        counter = new MonitoredCounter("food", "");
        this.baos = new ByteArrayOutputStream();
        this.pcpCS = new ByteCountingOutputStream(this.baos, counter);
        /*
         * this 'starts' the registry so that the call to shutdown() in the tearDown method will
         * clear the state for the next unit test
         */
        MonitorableRegistry.getMonitorables();
    }

    @SuppressWarnings("deprecation")
	protected void tearDown() throws Exception {
        counter.reset();
        this.baos = null;
        this.pcpCS = null;
        super.tearDown();
        MonitorableRegistry.shutdown();
    }

    public void testWriteInt() throws IOException {
        assertEquals(0, counter.get().longValue());
        this.pcpCS.write(5);// write an integer ( 4 bytes)
        assertEquals(4, counter.get().longValue());

    }

    public void testWriteByteArray() throws IOException {
        assertEquals(0, counter.get().longValue());
        String ex = "Nothing is impossible in this world except rizwan winning a lottery";
        byte[] buf = ex.getBytes();
        this.pcpCS.write(buf);
        assertEquals(buf.length, counter.get().longValue());
    }

    public void testWriteByteArrayIntInt() throws IOException {
        assertEquals(0, counter.get().longValue());
        String ex = "Nothing is impossible in this world except rizwan winning a lottery";
        byte[] buf = ex.getBytes();
        this.pcpCS.write(buf, 0, 10);
        assertEquals(10, counter.get().longValue());
    }

}
