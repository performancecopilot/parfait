package com.custardsource.parfait.dxm;

import java.util.Set;

class HashingIdentifierSource implements IdentifierSource {
    @Override
    public int calculateId(String name, Set<Integer> usedIds) {
        int value = name.hashCode();
        // Math.abs(MIN_VALUE) == MIN_VALUE, better deal with that just in case...
        if (value == Integer.MIN_VALUE) {
            value = 0;
        }
        value = Math.abs(value);
        while (usedIds.contains(value)) {
            if (value == Integer.MAX_VALUE) {
                value = 0;
            } else {
                value++;
            }
        }
        return value;
    }
}