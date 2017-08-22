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

package io.pcp.parfait.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import io.pcp.parfait.DynamicMonitoringView;
import io.pcp.parfait.MonitorableRegistry;
import io.pcp.parfait.MonitoringView;
import io.pcp.parfait.dxm.IdentifierSourceSet;
import io.pcp.parfait.dxm.PcpMmvWriter;
import io.pcp.parfait.pcp.PcpMonitorBridge;
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

    @NotNull
    private String pcpName;

    private String registryName = "dropwizard-default";

    private Map<String, String> replacements = new LinkedHashMap<>();

    private long quietPeriod = DynamicMonitoringView.defaultQuietPeriod();

    @NotNull
    private Integer clusterIdentifier;

    @JsonProperty
    public String getPrefix() {
        return prefix;
    }

    @JsonProperty
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @JsonProperty
    public String getPcpName() {
        return pcpName;
    }

    @JsonProperty
    public void setPcpName(String pcpName) {
        this.pcpName = pcpName;
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

    @JsonProperty
    public long getQuietPeriod() {
        return quietPeriod;
    }

    @JsonProperty
    public void setQuietPeriod(long quietPeriod) {
        this.quietPeriod = quietPeriod;
    }

    public Integer getClusterIdentifier() {
        return clusterIdentifier;
    }

    public void setClusterIdentifier(Integer clusterIdentifier) {
        this.clusterIdentifier = clusterIdentifier;
    }

    @Override
    public ScheduledReporter build(MetricRegistry metricRegistry) {
        MetricAdapterFactory metricAdapterFactory = new MetricAdapterFactoryImpl(
                new DefaultMetricDescriptorLookup(),
                new DefaultMetricNameTranslator(getReplacements()));

        MonitorableRegistry monitorableRegistry = MonitorableRegistry.getNamedInstance(getRegistryName());

        PcpMmvWriter pcpMmvWriter = new PcpMmvWriter(getPcpName(), IdentifierSourceSet.DEFAULT_SET);
        pcpMmvWriter.setClusterIdentifier(getClusterIdentifier());

        MonitoringView monitoringView = new PcpMonitorBridge(pcpMmvWriter);

        DynamicMonitoringView dynamicMonitoringView = new DynamicMonitoringView(monitorableRegistry, monitoringView, quietPeriod);

        return new ParfaitReporter(metricRegistry,
                monitorableRegistry,
                dynamicMonitoringView,
                metricAdapterFactory,
                getRateUnit(),
                getDurationUnit(),
                getFilter(),
                getPrefix());
    }
}
