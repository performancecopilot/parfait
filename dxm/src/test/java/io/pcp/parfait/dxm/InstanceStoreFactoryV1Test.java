package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.InstanceV1.InstanceStoreV1;
import io.pcp.parfait.dxm.PcpMmvWriter.Store;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class InstanceStoreFactoryV1Test {

    private static final String STORE_NAME = "mystore";

    @Test
    public void createNewInstanceStoreShouldReturnACorrectlyConstructedInstanceStore() {
        IdentifierSourceSet identifierSourceSet = mock(IdentifierSourceSet.class);
        InstanceDomain instanceDomain = mock(InstanceDomain.class);

        InstanceStoreFactory instanceStoreFactory = new InstanceStoreFactoryV1(identifierSourceSet);

        Store<Instance> actual = instanceStoreFactory.createNewInstanceStore(STORE_NAME, instanceDomain);
        Store<Instance> expected = new InstanceStoreV1(identifierSourceSet, STORE_NAME, instanceDomain);

        assertReflectionEquals(expected, actual);
    }

}