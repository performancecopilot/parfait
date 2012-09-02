package com.custardsource.parfait.dxm;

import java.util.Set;

public interface IdentifierSource {
    int calculateId(String name, Set<Integer> usedIds);
}