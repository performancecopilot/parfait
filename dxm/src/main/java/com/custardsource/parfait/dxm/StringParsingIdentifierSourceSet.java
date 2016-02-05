package com.custardsource.parfait.dxm;

import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

public class StringParsingIdentifierSourceSet implements IdentifierSourceSet {
    private final IdentifierSourceSet fallbacks;
    private final IdentifierSource metricSource;
    private final InstanceDomainIdentifierSource instanceDomainSource;

    public StringParsingIdentifierSourceSet(Iterable<String> instanceData,
            Iterable<String> metricData) {
        this(instanceData, metricData, IdentifierSourceSet.DEFAULT_SET);
    }

    public StringParsingIdentifierSourceSet(Iterable<String> instanceData,
            Iterable<String> metricData, IdentifierSourceSet fallbacks) {
        this.fallbacks = fallbacks;
        metricSource = parseMetrics(metricData);
        instanceDomainSource = parseInstances(instanceData);
    }

    private IdentifierSource parseMetrics(Iterable<String> metricData) {
        final BiMap<String, Integer> allocations = HashBiMap.<String, Integer>create();

        int lineNumber = 0;
        for (String currentLine : metricData) {
            lineNumber++;
            if (!(currentLine.trim().isEmpty() || currentLine.trim().startsWith("#"))) {
                parseAllocation(allocations, lineNumber, currentLine);
            }
        }
        return new FixedValueIdentifierSource(allocations, fallbacks.metricSource());
    }

    private InstanceDomainIdentifierSource parseInstances(Iterable<String> instanceData) {
        final Pattern startsWithBlank = Pattern.compile("^\\s", 1);
        final BiMap<String, Integer> allocations = HashBiMap.create();
        final Map<String, IdentifierSource> instanceSources = Maps.newHashMap();

        BiMap<String, Integer> currentInstanceAllocations = HashBiMap.create();

        String currentDomain = null;
        int lineNumber = 0;

        for (String currentLine : instanceData) {
            lineNumber++;
            if (isBlankOrComment(currentLine)) {
                // Do nothing
            } else if (startsWithBlank.matcher(currentLine).find()) {
                // This is an instance, not an indom
                if (currentDomain == null) {
                    throw new IllegalArgumentException(
                            "Error parsing line "
                                    + lineNumber
                                    + " of input; leading whitespace should only be used for instance values under an instance domain");
                }
                parseAllocation(currentInstanceAllocations, lineNumber, currentLine);
            } else {
                if (currentDomain != null) {
                    instanceSources.put(currentDomain, new FixedValueIdentifierSource(
                            currentInstanceAllocations, fallbacks.instanceSource(currentDomain)));
                }
                currentDomain = parseAllocation(allocations, lineNumber, currentLine);
                currentInstanceAllocations = HashBiMap.create();
            }
        }

        if (currentDomain != null) {
            instanceSources.put(currentDomain, new FixedValueIdentifierSource(
                    currentInstanceAllocations, fallbacks.instanceSource(currentDomain)));
        }

        return new InstanceDomainIdentifierSource(allocations, instanceSources, fallbacks
                .instanceDomainSource());
    }

    private static boolean isBlankOrComment(String currentLine) {
        return (currentLine.trim().isEmpty()) || (currentLine.trim().startsWith("#")); 
	}

	@Override
    public IdentifierSource metricSource() {
        return metricSource;
    }

    @Override
    public IdentifierSource instanceDomainSource() {
        return instanceDomainSource;
    }

    @Override
    public IdentifierSource instanceSource(String domain) {
        return instanceDomainSource.getInstanceSource(domain);
    }

    private static String parseAllocation(final BiMap<String, Integer> allocations, int lineNumber,
            String currentLine) {
        String[] elements = currentLine.trim().split("\\s+");
        if (elements.length != 2) {
            throw new IllegalArgumentException("Error parsing line " + lineNumber
                    + " of input; should have two columns in format <name><whitespace><id>");
        }
        String metricName = elements[0];
        Integer id = null;
        try {
            id = Integer.valueOf(elements[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error parsing line " + lineNumber
                    + " of input; identifier " + metricName + " has unparseable ID string '"
                    + elements[1] + "'");
        }
        if (allocations.containsValue(id)) {
            throw new IllegalArgumentException("Error parsing line " + lineNumber
                    + " of input; identifier " + metricName + " has ID " + id
                    + " which is already in use for identifier " + allocations.inverse().get(id));
        }
        allocations.put(metricName, id);
        return metricName;
    }

    private class InstanceDomainIdentifierSource extends FixedValueIdentifierSource {
        private final Map<String, IdentifierSource> instanceSources;

        public InstanceDomainIdentifierSource(Map<String, Integer> reservedIds,
                Map<String, IdentifierSource> instanceSources, IdentifierSource fallback) {
            super(reservedIds, fallback);
            this.instanceSources = instanceSources;
        }

        public IdentifierSource getInstanceSource(String instanceDomain) {
            IdentifierSource found = instanceSources.get(instanceDomain);
            return (found == null) ? fallbacks.instanceSource(instanceDomain) : found;
        }
    }
}
