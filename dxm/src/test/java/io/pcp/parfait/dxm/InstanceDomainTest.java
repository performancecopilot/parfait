/*
 * Copyright 2009-2017 Aconex
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpMmvWriter.Store;
import org.junit.Test;

import java.util.Collection;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstanceDomainTest {

    private static final String DOMAIN = "mydomain";

    @Test
    public void shouldReturnInstancesAssociatedWithTheStore() {
        InstanceStoreFactory instanceStoreFactory = mock(InstanceStoreFactory.class);
        Store<Instance> instanceStore = mock(Store.class);
        Collection<Instance> instances = singletonList(mock(Instance.class));

        when(instanceStoreFactory.createNewInstanceStore(eq(DOMAIN), any(InstanceDomain.class))).thenReturn(instanceStore);
        when(instanceStore.all()).thenReturn(instances);

        InstanceDomain instanceDomain = new InstanceDomain(DOMAIN, 111, instanceStoreFactory);

        assertThat(instanceDomain.getInstances(), is(instances));
    }
}