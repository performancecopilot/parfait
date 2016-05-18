package io.pcp.parfait.dxm;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

public class FixedValueIdentifierSource implements IdentifierSource {
    private final Map<String, Integer> reservedIds;
    private final IdentifierSource fallback;

    public FixedValueIdentifierSource(Map<String, Integer> reservedIds, IdentifierSource fallback) {
        this.reservedIds = reservedIds;
        this.fallback = fallback;
    }

    @Override
    public int calculateId(String name, Set<Integer> usedIds) {
        Integer reservedId = reservedIds.get(name);
        if (reservedId == null || usedIds.contains(reservedId)) {
            return fallback.calculateId(name, Sets.union(usedIds, new HashSet<Integer>(reservedIds
                    .values())));
        }
        return reservedId;
    }

}
