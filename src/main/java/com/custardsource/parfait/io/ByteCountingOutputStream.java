package com.custardsource.parfait.io;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ProxyOutputStream;

import com.custardsource.parfait.MonitoredCounter;

/**
 * Used to calculate the rate at which data is sent (downloaded) . When download outputstream
 * wrapped in a ByteCountingOutputStream it can update the counters as byte or byte[] chunks are sent ,
 * allowing us to truly see the actual rate of transfers happening.
 * 
 * @author rmohammed
 */

public class ByteCountingOutputStream extends ProxyOutputStream {

    private final MonitoredCounter byteCounter;

    /**
     * Constructs a ByteCountingOutputStream
     * 
     * @param out
     *            OutputStream to be wrapped
     * @param counter
     *            PCP metric counter to be updated
     */

    public ByteCountingOutputStream(OutputStream out, MonitoredCounter counter) {
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
