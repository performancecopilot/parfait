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

package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.semantics.Semantics;
import io.pcp.parfait.dxm.types.AbstractTypeHandler;
import io.pcp.parfait.dxm.types.MmvMetricType;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;

import static io.pcp.parfait.dxm.IdentifierSourceSet.DEFAULT_SET;
import static io.pcp.parfait.dxm.MmvVersion.MMV_VERSION1;
import static io.pcp.parfait.dxm.MmvVersion.MMV_VERSION2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static tech.units.indriya.AbstractUnit.ONE;

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

    @Test
    public void resetShouldClearStrings() throws Exception {
        pcpMmvWriterV1.reset();
        assertStringsCount(pcpMmvWriterV1, 0);
        pcpMmvWriterV1.addMetric(MetricName.parse("v1.string"), Semantics.DISCRETE, null, "test1");
        pcpMmvWriterV1.start();

        pcpMmvWriterV2.reset();
        assertStringsCount(pcpMmvWriterV2, 0);
        pcpMmvWriterV2.addMetric(MetricName.parse("v2.string"), Semantics.DISCRETE, null, "test2");
        pcpMmvWriterV2.start();

        waitForReload();

        assertMetric("mmv.v1.string", is("\"test1\""));
        assertMetric("mmv.v2.string", is("\"test2\""));

        assertStringsCount(pcpMmvWriterV1, 1);
        assertStringsCount(pcpMmvWriterV2, 2);

        pcpMmvWriterV1.reset();
        assertStringsCount(pcpMmvWriterV1, 0);

        pcpMmvWriterV2.reset();
        assertStringsCount(pcpMmvWriterV2, 0);
    }

    @Test
    public void metricUpdatesWhileResettingWriterShouldNotBeLost() throws Exception {
        // The order the metrics are written is non-deterministic because they're pulled out of a hash map, so
        // we must dynamically record their order.
        List<String> order = new ArrayList<>();

        pcpMmvWriterV1.reset();
        pcpMmvWriterV1.addMetric(MetricName.parse("value1"), Semantics.COUNTER, ONE, 1,
                new AbstractTypeHandler<Number>(MmvMetricType.I32, 4) {
                    public void putBytes(ByteBuffer buffer, Number value) {
                        order.add("value1");
                        buffer.putInt(value == null ? 0 : value.intValue());
                    }
                });
        pcpMmvWriterV1.addMetric(MetricName.parse("value2"), Semantics.COUNTER, ONE, 2,
                new AbstractTypeHandler<Number>(MmvMetricType.I32, 4) {
                    public void putBytes(ByteBuffer buffer, Number value) {
                        order.add("value2");
                        buffer.putInt(value == null ? 0 : value.intValue());
                    }
                });

        pcpMmvWriterV1.start();

        waitForReload();

        assertMetric("mmv.value1", is("1.000"));
        assertMetric("mmv.value2", is("2.000"));

        pcpMmvWriterV1.reset();

        // The idea here is that the 1st metric will be written immediately, but the 2nd will wait on the phaser to
        // write. This gives us time to update the 1st metric value. The sleep is needed to ensure the start() method
        // doesn't exit before updateMetric() is executed.
        Phaser phaser = new Phaser(2);

        pcpMmvWriterV1.addMetric(MetricName.parse("value1"), Semantics.COUNTER, ONE, 1,
                new AbstractTypeHandler<Number>(MmvMetricType.I32, 4) {
                    public void putBytes(ByteBuffer buffer, Number value) {
                        boolean isNotFirst = !"value1".equals(order.get(0));
                        if (isNotFirst) {
                            phaser.arriveAndAwaitAdvance();
                        }
                        buffer.putInt(value == null ? 0 : value.intValue());
                        if (isNotFirst) {
                            sleep(1_000);
                        }
                    }
                });
        pcpMmvWriterV1.addMetric(MetricName.parse("value2"), Semantics.COUNTER, ONE, 2,
                new AbstractTypeHandler<Number>(MmvMetricType.I32, 4) {
                    public void putBytes(ByteBuffer buffer, Number value) {
                        boolean isNotFirst = !"value2".equals(order.get(0));
                        if (isNotFirst) {
                            phaser.arriveAndAwaitAdvance();
                        }
                        buffer.putInt(value == null ? 0 : value.intValue());
                        if (isNotFirst) {
                            sleep(1_000);
                        }
                    }
                });

        CountDownLatch startDone = new CountDownLatch(1);

        new Thread(() -> {
            try {
                pcpMmvWriterV1.start();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                startDone.countDown();
            }
        }).start();

        // Will not continue till after the 1st metric has been written
        phaser.arriveAndAwaitAdvance();

        pcpMmvWriterV1.updateMetric(MetricName.parse(order.get(0)), 10);

        startDone.await();

        waitForReload();

        assertMetric("mmv." + order.get(0), is("10.000"));
    }

    private void assertMetric(String metricName, Matcher<String> expectedValue) throws Exception {
        String actual = pcpClient.getMetric(metricName);
        assertThat(actual, expectedValue);
    }

    private void assertStringsCount(PcpMmvWriter writer, int expectedCount) throws NoSuchFieldException, IllegalAccessException {
        Field field = PcpMmvWriter.class.getDeclaredField("stringStore");
        field.setAccessible(true);
        PcpString.PcpStringStore stringStore = (PcpString.PcpStringStore) field.get(writer);
        assertEquals(expectedCount, stringStore.getStrings().size());
    }

    private void waitForReload() throws InterruptedException {
        Thread.sleep(1000);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
