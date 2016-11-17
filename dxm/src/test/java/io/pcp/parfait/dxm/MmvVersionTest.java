package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.InstanceDomain.InstanceDomainStore;
import io.pcp.parfait.dxm.PcpMetricInfoV1.MetricInfoStoreV1;
import io.pcp.parfait.dxm.PcpMetricInfoV2.MetricInfoStoreV2;
import io.pcp.parfait.dxm.PcpMmvWriter.Store;
import io.pcp.parfait.dxm.PcpString.PcpStringStore;
import org.junit.Test;

import static io.pcp.parfait.dxm.MmvVersion.MMV_VERSION1;
import static io.pcp.parfait.dxm.MmvVersion.MMV_VERSION2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class MmvVersionTest {

    private static final int VERSION_1 = 1;
    private static final int VERSION_2 = 2;
    private static final int MMV1_NAME_LIMIT = 63;
    private static final int MMV1_DOMAIN_LIMIT = 63;
    private static final int MMV2_NAME_LIMIT = 255;
    private static final int MMV2_INSTANCE_LIMIT = 255;

    @Test
    public void mmvVersion1_shouldReturnTheCorrectVersion() {
        assertThat(MMV_VERSION1.getVersion(), is(VERSION_1));
    }

    @Test
    public void mmvVersion1_shouldCreateAVersion1MetricStore() {
        IdentifierSourceSet identifierSourceSet = mock(IdentifierSourceSet.class);

        Store<PcpMetricInfo> actual = MMV_VERSION1.createMetricInfoStore(identifierSourceSet, null);
        Store<PcpMetricInfo> expected = new MetricInfoStoreV1(identifierSourceSet);

        assertReflectionEquals(expected, actual);
    }

    @Test
    public void mmvVersion1_shouldCreateAnInstanceDomainStoreWithVersion1InstanceStoreFactory() {
        IdentifierSourceSet identifierSourceSet = mock(IdentifierSourceSet.class);

        Store<InstanceDomain> actual = MMV_VERSION1.createInstanceDomainStore(identifierSourceSet,null);
        Store<InstanceDomain> expected = new InstanceDomainStore(
                identifierSourceSet, new InstanceStoreFactoryV1(identifierSourceSet)
        );

        assertReflectionEquals(expected, actual);
    }

    @Test
    public void mmvVersion1_shouldCreateAVersion1MetricValidator() {
        MetricNameValidator actual = MMV_VERSION1.createMetricNameValidator();
        MetricNameValidator expected = new MetricNameValidator(MMV1_NAME_LIMIT, MMV1_DOMAIN_LIMIT);

        assertReflectionEquals(expected, actual);
    }

    @Test
    public void mmvVersion2_shouldReturnTheCorrectVersion() {
        assertThat(MMV_VERSION2.getVersion(), is(VERSION_2));
    }

    @Test
    public void mmvVersion2_shouldCreateAVersion1MetricStore() {
        IdentifierSourceSet identifierSourceSet = mock(IdentifierSourceSet.class);
        PcpStringStore stringStore = mock(PcpStringStore.class);

        Store<PcpMetricInfo> actual = MMV_VERSION2.createMetricInfoStore(identifierSourceSet, stringStore);
        Store<PcpMetricInfo> expected = new MetricInfoStoreV2(identifierSourceSet, stringStore);

        assertReflectionEquals(expected, actual);
    }

    @Test
    public void mmvVersion2_shouldCreateAnInstanceDomainStoreWithVersion2InstanceStoreFactory() {
        IdentifierSourceSet identifierSourceSet = mock(IdentifierSourceSet.class);
        PcpStringStore stringStore = mock(PcpStringStore.class);

        Store<InstanceDomain> actual = MMV_VERSION2.createInstanceDomainStore(identifierSourceSet, stringStore);
        Store<InstanceDomain> expected = new InstanceDomainStore(
                identifierSourceSet, new InstanceStoreFactoryV2(identifierSourceSet, stringStore)
        );

        assertReflectionEquals(expected, actual);
    }

    @Test
    public void mmvVersion2_shouldCreateAVersionMetricValidator() {
        MetricNameValidator actual = MMV_VERSION2.createMetricNameValidator();
        MetricNameValidator expected = new MetricNameValidator(MMV2_NAME_LIMIT, MMV2_INSTANCE_LIMIT);

        assertReflectionEquals(expected, actual);
    }

}