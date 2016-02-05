package com.custardsource.parfait.io;

import com.custardsource.parfait.Counter;
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
