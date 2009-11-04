package com.custardsource.parfait.dxm;

import java.util.Set;

interface IdentifierSource {
    int calculateId(String name, Set<Integer> usedIds);
}