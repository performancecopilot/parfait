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

import java.util.Set;

public class HashingIdentifierSource implements IdentifierSource {
    private final int allowedIdentifierCount;

    public HashingIdentifierSource(int allowedIdentifierCount) {
        this.allowedIdentifierCount = allowedIdentifierCount;
    }

    @Override
    public int calculateId(String name, Set<Integer> usedIds) {
        if (usedIds.size() == this.allowedIdentifierCount) {
            throw new IllegalStateException("All identifiers in use; cannot assign another");
        }
        int value = name.hashCode();
        // Math.abs(MIN_VALUE) == MIN_VALUE, better deal with that just in case...
        if (value == Integer.MIN_VALUE) {
            value = 0;
        }
        value = Math.abs(value) % allowedIdentifierCount;
        while (usedIds.contains(value)) {
            value++;
            if (value == allowedIdentifierCount) {
                value = 0;
            }
        }
        return value;
    }
}