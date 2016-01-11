package com.custardsource.parfait.dropwizard;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultMetricNameTranslator implements MetricNameTranslator {

    private Map<String, String> replacements = new LinkedHashMap<>();

    public DefaultMetricNameTranslator(Map<String, String> replacements) {
        this.replacements = replacements;
    }

    @Override
    public String translate(String name) {
        String translatedName = name;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            translatedName = translatedName.replaceAll(entry.getKey(), entry.getValue());
        }
        return translatedName;
    }
}
