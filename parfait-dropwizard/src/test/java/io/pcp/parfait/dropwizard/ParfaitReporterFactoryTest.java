package io.pcp.parfait.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.metrics.ConsoleReporterFactory;
import io.dropwizard.metrics.CsvReporterFactory;
import io.dropwizard.metrics.MetricsFactory;
import io.dropwizard.metrics.Slf4jReporterFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.Validation;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class ParfaitReporterFactoryTest {

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final ConfigurationFactory<MetricsFactory> factory =
            new ConfigurationFactory<>(MetricsFactory.class,
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
