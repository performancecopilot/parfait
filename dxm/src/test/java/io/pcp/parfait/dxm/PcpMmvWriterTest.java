package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpMmvWriter.Store;
import io.pcp.parfait.dxm.PcpString.PcpStringStore;
import io.pcp.parfait.dxm.semantics.Semantics;
import io.pcp.parfait.dxm.types.MmvMetricType;
import io.pcp.parfait.dxm.types.TypeHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.ByteBuffer;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tec.units.ri.AbstractUnit.ONE;

public class PcpMmvWriterTest {

    private static final String SOME_METRIC_NAME = "some.metric";
    private static final int EXPECTED_LENGTH = 730;
    private static final int MOCK_PCP_METRIC_INFO_BYTE_SIZE = 33;
    private static final int MOCK_INSTANCE_DOMAIN_BYTE_SIZE = 11;
    private static final int MOCK_INSTANCE_BYTE_SIZE = 22;
    @Mock
    private ByteBufferFactory byteBufferFactory;
    @Mock
    private IdentifierSourceSet identifierSourceSet;
    @Mock
    private MmvVersion mmvVersion;
    @Mock
    private MetricNameValidator metricNameValidator;
    @Mock
    private Store<InstanceDomain> instanceDomainStore;
    @Mock
    private Store<PcpMetricInfo> metricInfoStore;
    @Mock
    private ByteBuffer byteBuffer;
    @Mock
    private TypeHandler<CustomType> customTypeTypeHandler2;
    private PcpMmvWriter pcpMmvWriter;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        when(mmvVersion.createMetricNameValidator()).thenReturn(metricNameValidator);
        when(mmvVersion.createInstanceDomainStore(eq(identifierSourceSet), any(PcpStringStore.class))).thenReturn(instanceDomainStore);
        when(mmvVersion.createMetricInfoStore(eq(identifierSourceSet), any(PcpStringStore.class))).thenReturn(metricInfoStore);

        when(byteBufferFactory.build(anyInt())).thenReturn(byteBuffer);

        pcpMmvWriter = new PcpMmvWriter(byteBufferFactory, identifierSourceSet, mmvVersion);
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
        PcpMetricInfo pcpMetricInfo = mock(PcpMetricInfo.class);

        when(customTypeTypeHandler.getMetricType()).thenReturn(MmvMetricType.DOUBLE);
        when(metricInfoStore.byName(SOME_METRIC_NAME)).thenReturn(pcpMetricInfo);
        doReturn(customTypeTypeHandler).when(pcpMetricInfo).getTypeHandler();
        when(byteBuffer.slice()).thenReturn(mock(ByteBuffer.class));

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
    @SuppressWarnings("unchecked")
    public void shouldBuildAByteBufferOfTheCorrectLength() throws IOException {
        InstanceDomain instanceDomain = mock(InstanceDomain.class);
        Instance instance = mock(Instance.class);
        PcpMetricInfo pcpMetricInfo = mock(PcpMetricInfo.class);

        when(pcpMetricInfo.getTypeHandler()).thenReturn(mock(TypeHandler.class));
        when(pcpMetricInfo.byteSize()).thenReturn(MOCK_PCP_METRIC_INFO_BYTE_SIZE);
        when(instanceDomain.getInstances()).thenReturn(singletonList(instance));
        when(instanceDomain.byteSize()).thenReturn(MOCK_INSTANCE_DOMAIN_BYTE_SIZE);
        when(instance.byteSize()).thenReturn(MOCK_INSTANCE_BYTE_SIZE);

        when(metricInfoStore.byName(SOME_METRIC_NAME)).thenReturn(pcpMetricInfo);
        when(metricInfoStore.all()).thenReturn(singletonList(pcpMetricInfo));
        when(instanceDomainStore.all()).thenReturn(singletonList(instanceDomain));
        when(byteBuffer.slice()).thenReturn(mock(ByteBuffer.class));

        pcpMmvWriter.addMetric(MetricName.parse(SOME_METRIC_NAME), Semantics.COUNTER, null, 1);
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

        verify(mmvVersion).createMetricInfoStore(eq(identifierSourceSet), any(PcpStringStore.class));
    }

    @Test
    public void shouldCreateAnInstanceDomainStoreWhenConstructed() {
        MmvVersion mmvVersion = mock(MmvVersion.class);
        IdentifierSourceSet identifierSourceSet = mock(IdentifierSourceSet.class);

        new PcpMmvWriter(mock(ByteBufferFactory.class), identifierSourceSet, mmvVersion);

        verify(mmvVersion).createInstanceDomainStore(eq(identifierSourceSet), any(PcpStringStore.class));
    }

    private class CustomType {}

}

