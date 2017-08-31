/*
 * Copyright 2009-2017 Aconex
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.pcp.parfait.pcp;

import java.util.Map;

import io.pcp.parfait.Monitorable;
import io.pcp.parfait.dxm.MetricName;
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
            if (!(currentLine.trim().isEmpty() || currentLine.trim().startsWith("#"))) {
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
