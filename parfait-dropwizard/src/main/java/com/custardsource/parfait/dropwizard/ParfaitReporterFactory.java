package com.custardsource.parfait.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.custardsource.parfait.MonitorableRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.metrics.BaseReporterFactory;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonTypeName("parfait")
public class ParfaitReporterFactory extends BaseReporterFactory {

    @NotNull
    private String prefix = "";

    private String registryName = "dropwizard-default";

    private Map<String, String> replacements = new LinkedHashMap<>();

    @JsonProperty
    public String getPrefix() {
        return prefix;
    }

    @JsonProperty
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @JsonProperty
    public String getRegistryName() {
        return registryName;
    }

    @JsonProperty
    public void setRegistryName(String registryName) {
        this.registryName = registryName;
    }

    @JsonProperty
    public Map<String, String> getReplacements() {
        return replacements;
    }

    @JsonProperty
    public void setReplacements(Map<String, String> replacements) {
        this.replacements = replacements;
    }

    @Override
    public ScheduledReporter build(MetricRegistry metricRegistry) {
        MetricAdapterFactory metricAdapterFactory = new MetricAdapterFactoryImpl(
                new DefaultMetricDescriptorLookup(),
                new DefaultMetricNameTranslator(getReplacements()));

        return new ParfaitReporter(metricRegistry,
                MonitorableRegistry.getNamedInstance(getRegistryName()),
                metricAdapterFactory,
                getRateUnit(),
                getDurationUnit(),
                getFilter(),
                getPrefix());
    }
}
