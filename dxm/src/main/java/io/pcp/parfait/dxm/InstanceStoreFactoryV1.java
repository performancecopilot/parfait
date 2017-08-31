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

class InstanceStoreFactoryV1 implements InstanceStoreFactory {

    private IdentifierSourceSet instanceStores;

    InstanceStoreFactoryV1(IdentifierSourceSet instanceStores) {
        this.instanceStores = instanceStores;
    }

    @Override
    public Store<Instance> createNewInstanceStore(String name, InstanceDomain instanceDomain) {
        return new InstanceStoreV1(instanceStores, name, instanceDomain);
    }
}
