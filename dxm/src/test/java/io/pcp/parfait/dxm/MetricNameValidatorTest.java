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


import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricNameValidatorTest {

    private static final String METRIC_NAME_64_CHARS = "metric.name.that.is.64.characters.long.metric.name.that.is.64.ch";
    private static final String METRIC_NAME_63_CHARS = "metric.name.that.is.63.characters.long.metric.name.that.is.63.c";
    private static final String INSTANCE_NAME_64_CHARS = "instance.name.of.64.characters.instance.name.of.64.characters.in";
    private static final String INSTANCE_NAME_63_CHARS = "instance.name.of.63.characters.instance.name.of.63.characters.i";
    private static final int NAME_LIMIT = 63;
    private static final int DOMAIN_LIMIT = 63;
    private MetricNameValidator validator;

    @Before
    public void setUp() throws Exception {
        validator = new MetricNameValidator(NAME_LIMIT, DOMAIN_LIMIT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRaiseAnErrorIfTheMetricNameIsLongerThanTheAllowedLength() {
        MetricName metricName = mock(MetricName.class);

        when(metricName.getMetric()).thenReturn(METRIC_NAME_64_CHARS);

        validator.validateNameConstraints(metricName);
    }

    @Test
    public void shouldNotRaiseAnErrorIfTheMetricNameIsUnderTheLimit() {
        MetricName metricName = mock(MetricName.class);

        when(metricName.getMetric()).thenReturn(METRIC_NAME_63_CHARS);

        validator.validateNameConstraints(metricName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRaiseAnErrorIfTheMetricHasAnInstanceAndItsNameIsLongerThanTheAllowedLength() {
        MetricName metricName = mock(MetricName.class);

        when(metricName.getMetric()).thenReturn("");
        when(metricName.hasInstance()).thenReturn(true);
        when(metricName.getInstance()).thenReturn(INSTANCE_NAME_64_CHARS);

        validator.validateNameConstraints(metricName);
    }

    @Test
    public void shouldNotRaiseAnErrorIfTheMetricHasAnInstanceAndItsNameIsShorterThanTheAllowedLength() {
        MetricName metricName = mock(MetricName.class);

        when(metricName.getMetric()).thenReturn("");
        when(metricName.hasInstance()).thenReturn(true);
        when(metricName.getInstance()).thenReturn(INSTANCE_NAME_63_CHARS);

        validator.validateNameConstraints(metricName);

    }


}