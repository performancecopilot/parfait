package io.pcp.parfait.dxm;


import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MmvV1MetricNameValidatorTest {

    private static final String METRIC_NAME_64_CHARS = "metric.name.that.is.64.characters.long.metric.name.that.is.64.ch";
    private static final String METRIC_NAME_63_CHARS = "metric.name.that.is.63.characters.long.metric.name.that.is.63.c";
    public static final String INSTANCE_NAME_64_CHARS = "instance.name.of.64.characters.instance.name.of.64.characters.in";
    public static final String INSTANCE_NAME_63_CHARS = "instance.name.of.63.characters.instance.name.of.63.characters.i";
    private MmvV1MetricNameValidator validator;

    @Before
    public void setUp() throws Exception {
        validator = new MmvV1MetricNameValidator();
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