package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.semantics.Semantics;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.pcp.parfait.dxm.IdentifierSourceSet.DEFAULT_SET;
import static io.pcp.parfait.dxm.MmvVersion.MMV_VERSION1;
import static io.pcp.parfait.dxm.MmvVersion.MMV_VERSION2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static tec.units.ri.AbstractUnit.ONE;

public class PcpMmvWriterIntegrationTest {

    private static PcpClient pcpClient;
    private static PcpMmvWriter pcpMmvWriterV1;
    private static PcpMmvWriter pcpMmvWriterV2;

    @BeforeClass
    public static void setUp() throws Exception {
        pcpClient = new PcpClient();
        pcpMmvWriterV1 = new PcpMmvWriter("test1", DEFAULT_SET, MMV_VERSION1);
        pcpMmvWriterV2 = new PcpMmvWriter("test2", DEFAULT_SET, MMV_VERSION2);
    }

    @Test
    public void bothMmvVersionsShouldSupportAddingAMetric() throws Exception {
        pcpMmvWriterV1.reset();
        pcpMmvWriterV1.addMetric(MetricName.parse("v1.integer"), Semantics.COUNTER, ONE, 3);
        pcpMmvWriterV1.start();

        pcpMmvWriterV2.reset();
        pcpMmvWriterV2.addMetric(MetricName.parse("v2.integer"), Semantics.COUNTER, ONE, 3);
        pcpMmvWriterV2.start();

        waitForReload();

        assertMetric("mmv.v1.integer", is("3.000"));
        assertMetric("mmv.v2.integer", is("3.000"));
    }

    @Test
    public void mmvVersion2ShouldSupportMetricNamesOver63Characters() throws Exception {
        pcpMmvWriterV2.reset();
        pcpMmvWriterV2.addMetric(MetricName.parse("v2.metric.that.is.longer.than.63.characters.v2.metric.that.is.longer.than.63.characters"), Semantics.COUNTER, ONE, 10);
        pcpMmvWriterV2.start();

        waitForReload();

        assertMetric("mmv.v2.metric.that.is.longer.than.63.characters.v2.metric.that.is.longer.than.63.characters", is("10.000"));
    }

    @Test
    public void mmvVersion2ShouldSupportInstanceNamesOver63Characters() throws Exception {
        pcpMmvWriterV2.reset();
        pcpMmvWriterV2.addMetric(MetricName.parse("v2.integer[instance_name_over_63_characters_instance_name_over_63_characters_instance]"), Semantics.COUNTER, ONE, 11);
        pcpMmvWriterV2.start();

        waitForReload();

        assertMetric("mmv.v2.integer[instance_name_over_63_characters_instance_name_over_63_characters_instance]", is("11.000"));
    }

    private void assertMetric(String metricName, Matcher<String> expectedValue) throws Exception {
        String actual = pcpClient.getMetric(metricName);
        assertThat(actual, expectedValue);
    }

    private void waitForReload() throws InterruptedException {
        Thread.sleep(1000);
    }

}