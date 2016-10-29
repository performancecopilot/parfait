package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpMmvWriter.Store;
import org.junit.Test;

import java.util.Collection;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstanceDomainTest {

    private static final String DOMAIN = "mydomain";

    @Test
    public void shouldReturnInstancesAssociatedWithTheStore() {
        InstanceStoreFactory instanceStoreFactory = mock(InstanceStoreFactory.class);
        InstanceDomain instanceDomain = new InstanceDomain(DOMAIN, 111, instanceStoreFactory);
        Store<Instance> instanceStore = mock(Store.class);
        Collection<Instance> instances = singletonList(mock(Instance.class));

        when(instanceStoreFactory.createNewInstanceStore(DOMAIN, instanceDomain)).thenReturn(instanceStore);
        when(instanceStore.all()).thenReturn(instances);

        assertThat(instanceDomain.getInstances(), is(instances));
    }
}