package com.custardsource.parfait.dxm;

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
