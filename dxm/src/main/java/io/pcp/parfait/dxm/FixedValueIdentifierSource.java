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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

public class FixedValueIdentifierSource implements IdentifierSource {
    private final Map<String, Integer> reservedIds;
    private final IdentifierSource fallback;

    public FixedValueIdentifierSource(Map<String, Integer> reservedIds, IdentifierSource fallback) {
        this.reservedIds = reservedIds;
        this.fallback = fallback;
    }

    @Override
    public int calculateId(String name, Set<Integer> usedIds) {
        Integer reservedId = reservedIds.get(name);
        if (reservedId == null || usedIds.contains(reservedId)) {
            return fallback.calculateId(name, Sets.union(usedIds, new HashSet<Integer>(reservedIds
                    .values())));
        }
        return reservedId;
    }

}
