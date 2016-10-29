package io.pcp.parfait.dxm;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.mockito.Mockito.mock;

public class PcpMetricInfoV2Test {

    @Test(expected = RuntimeException.class)
    public void writeToMmvShouldRaiseAnErrorAsItIsNotImplementedYet() {
        PcpMetricInfoV2 pcpMetricInfoV2 = new PcpMetricInfoV2("name", 1, mock(PcpString.class));

        pcpMetricInfoV2.writeToMmv(mock(ByteBuffer.class));
    }
}