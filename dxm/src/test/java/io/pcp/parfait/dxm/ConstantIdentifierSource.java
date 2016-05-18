package io.pcp.parfait.dxm;

import java.util.Set;

public class ConstantIdentifierSource implements IdentifierSource {
    private final int returnValue;

    public ConstantIdentifierSource(int returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public int calculateId(String name, Set<Integer> usedIds) {
        return returnValue;
    }
}
