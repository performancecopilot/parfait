package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpMetricInfoV2.MetricInfoStoreV2;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.nio.ByteBuffer;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class PcpMetricInfoV2Test {

    private PcpMetricInfoV2 pcpMetricInfoV2;

    @Before
    public void setUp() throws Exception {
        pcpMetricInfoV2 = new PcpMetricInfoV2("name", 1, mock(PcpString.class));
    }

    @Test(expected = RuntimeException.class)
    public void writeToMmvShouldRaiseAnErrorAsItIsNotImplementedYet() {
        pcpMetricInfoV2.writeToMmv(mock(ByteBuffer.class));
    }

    @Test(expected = RuntimeException.class)
    public void byteSizeShouldRaiseAnErrorAsItIsNotImplementedYet() {
        pcpMetricInfoV2.byteSize();
    }

    @Test
    public void metricInfoStoreShouldCreateANewPcpMetricInfoV2() {
        IdentifierSourceSet identifierSourceSet = mock(IdentifierSourceSet.class);
        PcpMmvWriter pcpMmvWriter = mock(PcpMmvWriter.class);
        PcpString pcpString = mock(PcpString.class);
        IdentifierSource identifierSource = mock(IdentifierSource.class);

        when(identifierSourceSet.metricSource()).thenReturn(identifierSource);
        when(identifierSource.calculateId(eq("my.metric"), ArgumentMatchers.<Integer>anySet())).thenReturn(123);
        when(pcpMmvWriter.createPcpString("my.metric")).thenReturn(pcpString);

        MetricInfoStoreV2 metricInfoStoreV2 = new MetricInfoStoreV2(identifierSourceSet, pcpMmvWriter);

        PcpMetricInfo actual = metricInfoStoreV2.byName("my.metric");

        PcpMetricInfoV2 expected = new PcpMetricInfoV2("my.metric", 123, pcpString);

        assertReflectionEquals(expected, actual);
    }
}