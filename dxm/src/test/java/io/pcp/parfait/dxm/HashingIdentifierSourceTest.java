package io.pcp.parfait.dxm;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class HashingIdentifierSourceTest {
    @Test
    public void zeroHashShouldProduceZeroIdentifier() {
        assertHashEquals(stringWhichHashesTo(0), 0);
    }

    @Test
    public void positiveHashShouldProducePositiveIdentifier() {
        assertHashEquals(stringWhichHashesTo(1), 1);
    }

    @Test
    public void hashToMaxintShouldProduceZeroIdentifier() {
        assertHashEquals(stringWhichHashesTo(Integer.MAX_VALUE), 0);
    }

    @Test
    public void hashToNearlyMaxintShouldProduceNearlyMaxint() {
        assertHashEquals(stringWhichHashesTo(Integer.MAX_VALUE - 1), Integer.MAX_VALUE - 1);
    }

    @Test
    public void hashShouldIncrementOnCollision() {
        assertHashEquals(stringWhichHashesTo(1), 2, Sets.newHashSet(1));
    }

    @Test
    public void maxintHashShouldWrapToZeroOnCollision() {
        assertHashEquals(stringWhichHashesTo(Integer.MAX_VALUE), 0, Sets
                .newHashSet(Integer.MAX_VALUE));
    }

    @Test
    public void negativeHashShouldProducePositiveIdentifier() {
        assertHashEquals(stringWhichHashesTo(-3), 3);
    }

    @Test
    public void minintHashShouldProduceZeroIdentifier() {
        assertHashEquals(stringWhichHashesTo(Integer.MIN_VALUE), 0);
    }

    @Test(expected = IllegalStateException.class)
    public void hashWithAllValuesUsedShouldThrow() {
        System.out.println(new HashingIdentifierSource(3).calculateId(stringWhichHashesTo(2), Sets.newHashSet(0, 1, 2)));
    }

    @Test
    public void hashValueShouldWrapToZeroWhenHashUsed() {
        assertHashEquals(stringWhichHashesTo(Integer.MAX_VALUE - 1), 0, Sets.newHashSet(Integer.MAX_VALUE - 1));
    }

    private void assertHashEquals(String input, int expectedOutput) {
        assertHashEquals(input, expectedOutput, new HashSet<Integer>());

    }

    private void assertHashEquals(String input, int expectedOutput, Set<Integer> existingIdentifiers) {
        assertHashEquals(input, expectedOutput, existingIdentifiers, Integer.MAX_VALUE);
    }

    private void assertHashEquals(String input, int expectedOutput, Set<Integer> existingIdentifiers, int identifierCount) {
        assertEquals(expectedOutput, new HashingIdentifierSource(identifierCount).calculateId(input,
                existingIdentifiers));
    }

    private static String stringWhichHashesTo(int desiredValue) {
        // Taken from http://blogs.sun.com/darcy/entry/string_unhashing?intcmp=2223
        long hash = desiredValue & ((1L << 32) - 1);
        StringBuilder result = new StringBuilder();
        while (hash > 0) {
            result.append((char) (hash % 31));
            hash /= 31;
        }
        return result.reverse().toString();
    }
}
