package com.custardsource.parfait.dxm;

import org.junit.Before;
import org.junit.Test;

public class PcpMmvWriterTest {

    private PcpMmvWriter pcpMmvWriter;

    @Before
    public void setUp(){
        pcpMmvWriter = new PcpMmvWriter(new InMemoryByteBufferFactory(), IdentifierSourceSet.DEFAULT_SET);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureThatClusterIdentifierIsRestrictedTo12BitsOnly() throws Exception {
        pcpMmvWriter.setClusterIdentifier(1<<13);
    }

    @Test
    public void ensureValid12BitIdentifierIsAllowed(){
        pcpMmvWriter.setClusterIdentifier(1);
    }

    @Test
    public void ensureBoundaryCaseOf12thBitIsOk(){
        pcpMmvWriter.setClusterIdentifier(1<<11);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNegativeNumbersAreTreatedWithAppropriateContempt() {
        pcpMmvWriter.setClusterIdentifier(-1);

    }

}

