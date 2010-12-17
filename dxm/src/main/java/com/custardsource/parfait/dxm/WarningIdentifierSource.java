package com.custardsource.parfait.dxm;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IdentifierSource} which logs a warning on every generation, and returns a dummy value.
 * Useful for generating a list of IDs which should later be explicity-provided.
 */
public class WarningIdentifierSource implements IdentifierSource {
    private static final Logger LOG = LoggerFactory.getLogger(WarningIdentifierSource.class);

    private final AtomicInteger nextId;

    public WarningIdentifierSource(int initialId) {
        nextId = new AtomicInteger(initialId);
    }

    @Override
    public int calculateId(String name, Set<Integer> usedIds) {
        LOG.warn("No identifier provided for value " + name + "; please specify an explicit ID");
        return nextId.getAndIncrement();
    }

}
