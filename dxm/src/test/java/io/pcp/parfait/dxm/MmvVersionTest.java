package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.InstanceDomain.InstanceDomainStore;
import io.pcp.parfait.dxm.PcpMetricInfoV1.MetricInfoStoreV1;
import io.pcp.parfait.dxm.PcpMmvWriter.Store;
import org.junit.Test;

import static io.pcp.parfait.dxm.MmvVersion.MMV_VERSION1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class MmvVersionTest {

    private static final int VERSION_1 = 1;
    private static final int MMV1_NAME_LIMIT = 63;
    private static final int MMV1_DOMAIN_LIMIT = 63;

    @Test
    public void mmvVersion1_shouldReturnTheCorrectVersion() {
        assertThat(MMV_VERSION1.getVersion(), is(VERSION_1));
    }

    @Test
    public void mmvVersion1_shouldCreateAVersion1MetricStore() {
        IdentifierSourceSet identifierSourceSet = mock(IdentifierSourceSet.class);

        Store<PcpMetricInfo> actual = MMV_VERSION1.createMetricInfoStore(identifierSourceSet);
        Store<PcpMetricInfo> expected = new MetricInfoStoreV1(identifierSourceSet);

        assertReflectionEquals(expected, actual);
    }

    @Test
    public void mmvVersion1_shouldCreateAnInstanceDomainStoreWithVersion1InstanceStoreFactory() {
        IdentifierSourceSet identifierSourceSet = mock(IdentifierSourceSet.class);

        Store<InstanceDomain> actual = MMV_VERSION1.createInstanceDomainStore(identifierSourceSet);
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

}