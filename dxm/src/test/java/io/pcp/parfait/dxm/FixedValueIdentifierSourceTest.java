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

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class FixedValueIdentifierSourceTest {
    private static final String PREDEFINED_STRING = "this.that.theother";
    private static final int PREDEFINED_IDENTIFIER = 7;

    private static final int FIXED_FALLBACK = 0xFEEDaBEE;
    private static final IdentifierSource FALLBACK_SOURCE = new ConstantIdentifierSource(
            FIXED_FALLBACK);

    @Test
    public void shouldFallBackWhenNoValueProvided() {
        assertEquals(FIXED_FALLBACK, emptySource().calculateId(PREDEFINED_STRING,
                noExistingIdentifiers()));
    }

    @Test
    public void shouldReturnFixedValueWhenProvided() {
        assertEquals(PREDEFINED_IDENTIFIER, populatedSource().calculateId(PREDEFINED_STRING,
                noExistingIdentifiers()));
    }

    @Test
    public void shouldReturnFallBackWhenValueCollides() {
        assertEquals(FIXED_FALLBACK, populatedSource().calculateId(PREDEFINED_STRING,
                collidingIdentifiers()));
    }

    private IdentifierSource emptySource() {
        return buildSource(Maps.<String, Integer> newHashMap());
    }

    private IdentifierSource populatedSource() {
        return buildSource(ImmutableMap.of(PREDEFINED_STRING, PREDEFINED_IDENTIFIER));
    }

    private IdentifierSource buildSource(Map<String, Integer> mappings) {
        return new FixedValueIdentifierSource(mappings, FALLBACK_SOURCE);
    }

    private Set<Integer> noExistingIdentifiers() {
        return Collections.emptySet();
    }

    private Set<Integer> collidingIdentifiers() {
        return Collections.singleton(PREDEFINED_IDENTIFIER);
    }
}
