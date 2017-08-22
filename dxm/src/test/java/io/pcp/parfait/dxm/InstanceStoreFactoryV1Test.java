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