package com.custardsource.parfait.pcp;

import java.util.Map;

import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.dxm.MetricName;
import com.google.common.collect.Maps;

public class StringParsingTextSource implements TextSource {
    private final TextSource delegate;

    public StringParsingTextSource(Iterable<String> input, TextSource fallback) {
        delegate = new MapTextSource(fallback, parseMap(input));
    }

    private Map<String, String> parseMap(Iterable<String> input) {
        Map<String, String> output = Maps.newHashMap();
        int lineNumber = 0;
        for (String currentLine : input) {
            lineNumber++;
            if (!currentLine.trim().isEmpty()) {
                String[] elements = currentLine.trim().split("\t");
                if (elements.length != 2) {
                    throw new IllegalArgumentException(
                            "Error parsing line "
                                    + lineNumber
                                    + " of input; should have two tab-delimited columns in format <name>\\t<text>");
                }
                String metricName = elements[0];
                String text = elements[1];

                output.put(metricName, text);
            }
        }
        return output;
    }

    @Override
    public String getText(Monitorable<?> monitorable, MetricName mappedName) {
        return delegate.getText(monitorable, mappedName);
    }

}
