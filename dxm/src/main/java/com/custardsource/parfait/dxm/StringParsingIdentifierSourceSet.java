package com.custardsource.parfait.dxm;

import java.util.Map;
import java.util.regex.Pattern;

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
        final Map<String, Integer> allocations = Maps.newHashMap();

        int lineNumber = 0;
        for (String currentLine : metricData) {
            lineNumber++;
            if (!currentLine.trim().isEmpty()) {
                parseAllocation(allocations, lineNumber, currentLine);
            }
        }
        return new FixedValueIdentifierSource(allocations, fallbacks.metricSource());
    }

    private InstanceDomainIdentifierSource parseInstances(Iterable<String> instanceData) {
        final Pattern startsWithBlank = Pattern.compile("^\\s", 1);
        final Map<String, Integer> allocations = Maps.newHashMap();
        final Map<String, IdentifierSource> instanceSources = Maps.newHashMap();

        Map<String, Integer> currentInstanceAllocations = Maps.newHashMap();

        String currentDomain = null;
        int lineNumber = 0;

        for (String currentLine : instanceData) {
            lineNumber++;
            if (currentLine.trim().isEmpty()) {
                // Skip 
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
                currentInstanceAllocations = Maps.newHashMap();
            }
        }

        if (currentDomain != null) {
            instanceSources.put(currentDomain, new FixedValueIdentifierSource(
                    currentInstanceAllocations, fallbacks.instanceSource(currentDomain)));
        }

        return new InstanceDomainIdentifierSource(allocations, instanceSources, fallbacks
                .instanceDomainSource());
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

    private String parseAllocation(final Map<String, Integer> allocations, int lineNumber,
            String currentLine) {
        String[] elements = currentLine.trim().split("\\s+");
        if (elements.length != 2) {
            throw new IllegalArgumentException("Error parsing line " + lineNumber
                    + " of input; should have two columns in format <name><whitespace><id>");
        }
        String metricName = elements[0];
        Integer id = Integer.valueOf(elements[1]);
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
