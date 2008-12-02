package com.aconex.monitoring.pcp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;

import com.aconex.monitoring.MonitorableRegistry;
import com.aconex.monitoring.MonitoredValue;

public class PcpMonitorBridgeTest extends TestCase {
    private MonitoredValue<Boolean> boolanValue = null;

    private MonitoredValue<Integer> intValue = null;

    private MonitoredValue<Long> longValue = null;
    private MonitoredValue<Double> doubleValue = null;
    private MonitoredValue<String> stringValue = null;
    
    private MonitorableRegistry registry = new MonitorableRegistry();

    private PcpMonitorBridge pcp = null;

    public PcpMonitorBridgeTest() {
    }

    public void setUp() {
        boolanValue = new MonitoredValue<Boolean>("boolean.value", "boolean.value.desc", registry, true);
        intValue = new MonitoredValue<Integer>("int.value", "int.value.desc", registry, 1);
        longValue = new MonitoredValue<Long>("long.value", "long.value.desc", registry, 1l);
        doubleValue = new MonitoredValue<Double>("double.value", "double.value.desc", registry, 1d);
        stringValue = new MonitoredValue<String>("string.value", "string.value.desc", registry, "!");

        pcp = new PcpMonitorBridge("test", System.getProperty("java.io.tmpdir"), registry);

    }

    public void tearDown() {
        pcp.stop();
    }

    public void testSupportsAllTypes() throws IOException, InterruptedException {
        pcp.start();

        long generationNumber = checkHeaderFileFormat();

        checkDataValues(generationNumber);

        boolanValue.set(false);
        checkDataValues(generationNumber);

        boolanValue.set(true);
        checkDataValues(generationNumber);

        intValue.set(0);
        checkDataValues(generationNumber);

        intValue.set(Integer.MAX_VALUE);
        checkDataValues(generationNumber);

        intValue.set(Integer.MIN_VALUE);
        checkDataValues(generationNumber);

        intValue.set(1234567890);
        checkDataValues(generationNumber);

        longValue.set(0l);
        checkDataValues(generationNumber);

        longValue.set(Long.MAX_VALUE);
        checkDataValues(generationNumber);

        longValue.set(Long.MIN_VALUE);
        checkDataValues(generationNumber);

        longValue.set(1234567891012345679l);
        checkDataValues(generationNumber);

        doubleValue.set(0d);
        checkDataValues(generationNumber);

        doubleValue.set(Double.MAX_VALUE);
        checkDataValues(generationNumber);

        doubleValue.set(Double.MIN_VALUE);
        checkDataValues(generationNumber);

        doubleValue.set(Double.NEGATIVE_INFINITY);
        checkDataValues(generationNumber);

        doubleValue.set(Double.POSITIVE_INFINITY);
        checkDataValues(generationNumber);

        doubleValue.set(Double.NaN);
        checkDataValues(generationNumber);

        doubleValue.set(1234567891.012345679d);
        checkDataValues(generationNumber);

        stringValue.set("");
        checkDataValues(generationNumber);

        stringValue.set(createString(PcpMonitorBridge.MAX_STRING_LENGTH / 3));
        checkDataValues(generationNumber);

        stringValue.set(createString(PcpMonitorBridge.MAX_STRING_LENGTH - 1));
        checkDataValues(generationNumber);

        stringValue.set(createString(PcpMonitorBridge.MAX_STRING_LENGTH));
        checkDataValues(generationNumber);

        stringValue.set(createString(500));
        checkDataValues(generationNumber);
    }

    private String createString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(Math.max(1, i & 255));
        }
        return sb.toString();
    }

    private long checkHeaderFileFormat() throws IOException {
        LineNumberReader in = new LineNumberReader(new InputStreamReader(new FileInputStream(System
                .getProperty("java.io.tmpdir")
                + "/test.pcp.header"), PcpMonitorBridge.ENCODING));

        assertEquals("version=" + PcpMonitorBridge.PROTOCOL_VERSION, in.readLine());

        String generation = in.readLine();
        assertTrue(generation.startsWith("generation="));

        assertEquals("boolean.value=12,int", in.readLine());
        assertEquals("double.value=16,double", in.readLine());
        assertEquals("int.value=24,int", in.readLine());
        assertEquals("long.value=32,long", in.readLine());
        assertEquals("string.value=40,string", in.readLine());

        in.close();

        long generationNumber = Long.valueOf(StringUtils.substringAfter(generation, "="));
        return generationNumber;
    }

    private void checkDataValues(long generationNumber) throws IOException, InterruptedException {
        waitForQueueToEmpty();

        File dataFile = new File(System.getProperty("java.io.tmpdir") + "/test.pcp.data");
        FileInputStream in = new FileInputStream(dataFile);
        ByteBuffer dataBuffer = in.getChannel().map(MapMode.READ_ONLY, 0, dataFile.length());
        dataBuffer.order(ByteOrder.nativeOrder());
        in.close();

        assertEquals(generationNumber, dataBuffer.getLong(0));
        assertEquals(PcpMonitorBridge.PROTOCOL_VERSION, dataBuffer.get(8));

        assertEquals(boolanValue.get() ? 1 : 0, dataBuffer.getInt(12));
        assertEquals(doubleValue.get(), dataBuffer.getDouble(16));
        assertEquals((int) intValue.get(), dataBuffer.getInt(24));
        assertEquals((long) longValue.get(), dataBuffer.getLong(32));

        byte[] string = stringValue.get().getBytes(PcpMonitorBridge.ENCODING);
        dataBuffer.position(40);
        for (int i = 0; i < Math.min(string.length, PcpMonitorBridge.MAX_STRING_LENGTH - 1); i++) {
            assertEquals(string[i], dataBuffer.get());
        }
        assertEquals(0, dataBuffer.get());
    }

    private void waitForQueueToEmpty() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (pcp.hasUpdatesPending()) {
            if (System.currentTimeMillis() - startTime > 10000) {
                fail("Took too long to consume pending updates.");
            }
            Thread.sleep(10);
        }
    }
}
