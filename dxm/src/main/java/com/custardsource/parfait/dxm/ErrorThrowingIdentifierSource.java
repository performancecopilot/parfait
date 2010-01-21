package com.custardsource.parfait.dxm;

import java.util.Set;

/**
 * {@link IdentifierSource} which throws an error on every generation. Can be used as a 'fallback'
 * IdentifierSource when only explicitly-provided IDs should be used.
 */
public class ErrorThrowingIdentifierSource implements IdentifierSource {
    @Override
    public int calculateId(String name, Set<Integer> usedIds) {
        throw new UnsupportedOperationException("No identifier provided for value " + name
                + "; please specify an explicit ID");
    }

}
