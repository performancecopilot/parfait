package com.custardsource.parfait.dxm;

import java.util.Set;

class HashingIdentifierSource implements IdentifierSource {
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