package io.pcp.parfait.dxm;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tec.units.ri.AbstractUnit.ONE;

import java.io.IOException;
import java.nio.ByteBuffer;

import io.pcp.parfait.dxm.semantics.Semantics;
import io.pcp.parfait.dxm.types.MmvMetricType;
import io.pcp.parfait.dxm.types.TypeHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PcpMmvWriterTest {

    private static final String SOME_METRIC_NAME = "some.metric";
    private static final String INSTANCE_DOMAIN_NAME = "some";
    private static final int EXPECTED_LENGTH = 1016;
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
    public void ensureNegativeClustersAreTreatedWithAppropriateContempt() {
        pcpMmvWriter.setClusterIdentifier(-1);
    }

    @Test
    public void ensureValidProcessIdentifierIsAllowed() {
        pcpMmvWriter.setProcessIdentifier(23826);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNegativeProcessesAreTreatedWithAppropriateContempt() {
        pcpMmvWriter.setProcessIdentifier(-1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ensureUpdatesAreIgnoredAfterResetIsCalled() throws IOException {
        MetricName metricName = MetricName.parse(SOME_METRIC_NAME);
        TypeHandler<CustomType> customTypeTypeHandler = mock(TypeHandler.class);
        when(customTypeTypeHandler.getMetricType()).thenReturn(MmvMetricType.DOUBLE);
        pcpMmvWriter.registerType(CustomType.class, customTypeTypeHandler);
        pcpMmvWriter.start();
        pcpMmvWriter.reset();
        Mockito.reset(customTypeTypeHandler);
        pcpMmvWriter.addMetric(metricName, Semantics.COUNTER, ONE, new CustomType());
        pcpMmvWriter.updateMetric(metricName, new CustomType());
        verify(customTypeTypeHandler, never()).putBytes(any(ByteBuffer.class), any(CustomType.class));
    }

    @Test(expected = IllegalStateException.class)
    public void ensureSetPerMetricLockIsNotAllowedWhenWriterIsStarted() throws IOException {
        pcpMmvWriter.start();
        pcpMmvWriter.setPerMetricLock(true);
    }

    @Test
    public void ensureSetPerMetricLockIsAllowedAfterResetIsCalled() throws IOException {
        pcpMmvWriter.start();
        pcpMmvWriter.reset();
        pcpMmvWriter.setPerMetricLock(true);
    }

    @Test
    public void shouldBuildAByteBufferOfTheCorrectLength() throws IOException {
        ByteBufferFactory byteBufferFactory = mock(ByteBufferFactory.class);
        ByteBuffer byteBuffer = mock(ByteBuffer.class);
        IdentifierSourceSet identifierSourceSet = mock(IdentifierSourceSet.class);

        when(identifierSourceSet.metricSource()).thenReturn(mock(IdentifierSource.class));
        when(identifierSourceSet.instanceDomainSource()).thenReturn(mock(IdentifierSource.class));
        when(identifierSourceSet.instanceSource(INSTANCE_DOMAIN_NAME)).thenReturn(mock(IdentifierSource.class));
        when(byteBufferFactory.build(anyInt())).thenReturn(byteBuffer);
        when(byteBuffer.slice()).thenReturn(mock(ByteBuffer.class));

        PcpMmvWriter pcpMmvWriter = new PcpMmvWriter(byteBufferFactory, identifierSourceSet);
        pcpMmvWriter.addMetric(MetricName.parse(SOME_METRIC_NAME), Semantics.COUNTER, null, 1);
        pcpMmvWriter.addMetric(MetricName.parse("some[myinst].other_metric"), Semantics.COUNTER, null, 2);
        pcpMmvWriter.setMetricHelpText(SOME_METRIC_NAME, "Short help", "Long help");

        pcpMmvWriter.start();

        verify(byteBufferFactory).build(EXPECTED_LENGTH);
    }

    @Test
    public void shouldCreateAMetricNameValidatorWhenConstructed() {
        MmvVersion mmvVersion = mock(MmvVersion.class);

        new PcpMmvWriter(mock(ByteBufferFactory.class), mock(IdentifierSourceSet.class), mmvVersion);

        verify(mmvVersion).createMetricNameValidator();
    }

    @Test
    public void shouldCreateAMetricInfoStoreWhenConstructed() {
        MmvVersion mmvVersion = mock(MmvVersion.class);
        IdentifierSourceSet identifierSourceSet = mock(IdentifierSourceSet.class);

        new PcpMmvWriter(mock(ByteBufferFactory.class), identifierSourceSet, mmvVersion);

        verify(mmvVersion).createMetricInfoStore(identifierSourceSet);
    }

    @Test
    public void shouldCreateAnInstanceDomainStoreWhenConstructed() {
        MmvVersion mmvVersion = mock(MmvVersion.class);
        IdentifierSourceSet identifierSourceSet = mock(IdentifierSourceSet.class);

        new PcpMmvWriter(mock(ByteBufferFactory.class), identifierSourceSet, mmvVersion);

        verify(mmvVersion).createInstanceDomainStore(identifierSourceSet);
    }

    private class CustomType {}

}

