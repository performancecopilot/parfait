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
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.metrics.ConsoleReporterFactory;
import io.dropwizard.metrics.CsvReporterFactory;
import io.dropwizard.metrics.MetricsFactory;
import io.dropwizard.metrics.Slf4jReporterFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.Validation;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class ParfaitReporterFactoryTest {

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final YamlConfigurationFactory<MetricsFactory> factory =
            new YamlConfigurationFactory<>(MetricsFactory.class,
                    Validation.buildDefaultValidatorFactory().getValidator(),
                    objectMapper, "dw");

    private MetricsFactory config;

    @Before
    public void setUp() throws Exception {
        objectMapper.getSubtypeResolver().registerSubtypes(
                ConsoleReporterFactory.class,
                CsvReporterFactory.class,
                Slf4jReporterFactory.class,
                ParfaitReporterFactory.class);

        this.config = factory.build(new File(getClass().getResource("metric-app.yml").toURI()));
    }


    @Mock
    private MetricRegistry metricRegistry;

    @Test
    public void testBuildFromYamlConfiguration() throws Exception {
        ParfaitReporterFactory factory = (ParfaitReporterFactory) config.getReporters().get(0);
        assertEquals("dropwizard-default", factory.getRegistryName());
        assertNotNull(factory.getReplacements().get("io.dropwizard.jetty.MutableServletContextHandler"));

        ParfaitReporter reporter = (ParfaitReporter) factory.build(metricRegistry);
        assertNotNull(reporter);
    }
}
