package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpMmvWriter.Store;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class InstanceStoreFactoryV2Test {

    private static final String STORE_NAME = "mystore";

    @Test
    public void shouldCreateANewInstanceStoreV2() {
        IdentifierSourceSet identifierSourceSet = mock(IdentifierSourceSet.class);
        PcpMmvWriter pcpMmvWriter = mock(PcpMmvWriter.class);
        InstanceDomain instanceDomain = mock(InstanceDomain.class);

        InstanceStoreFactory instanceStoreFactory = new InstanceStoreFactoryV2(identifierSourceSet, pcpMmvWriter);

        Store<Instance> actual = instanceStoreFactory.createNewInstanceStore(STORE_NAME, instanceDomain);
        Store<Instance> expected = new InstanceV2.InstanceStoreV2(identifierSourceSet, STORE_NAME, instanceDomain, pcpMmvWriter);

        assertReflectionEquals(expected, actual);
    }

}