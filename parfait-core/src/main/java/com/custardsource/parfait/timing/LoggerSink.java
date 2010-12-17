package com.custardsource.parfait.timing;

import java.util.Collection;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class LoggerSink implements StepMeasurementSink {
    private final Logger logger;
    private final Map<Unit<?>, Unit<?>> normalizations = Maps.newConcurrentMap();

    public LoggerSink() {
        this(LoggerSink.class.getName());
    }

    public LoggerSink(String loggerName) {
        logger = LoggerFactory.getLogger(loggerName);
    }


    @Override
    public void handle(StepMeasurements measurements, int depth) {
        String depthText = buildDepthString(depth);
        String metricData = buildMetricDataString(measurements.getMetricInstances());
        logger.info(String.format("%s\t%s\t%s\t%s", depthText, measurements.getForwardTrace(), measurements
                .getBackTrace(), metricData));

    }

    private String buildDepthString(int depth) {
        return (depth > 0) ? "Nested (" + depth + ")" : "Top";
    }

    private String buildMetricDataString(Collection<MetricMeasurement> metricInstances) {
        String result = "";
        for (MetricMeasurement metric : metricInstances) {
            result += "\t" + buildSingleMetricResult(metric);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    String buildSingleMetricResult(MetricMeasurement metric) {
        Measure<?> ownTimeValue = metric.ownTimeValue();
        Measure<?> totalValue = metric.totalValue();
        Unit canonicalUnit = normalizations.get(metric.getMetricSource().getUnit());
        if (canonicalUnit == null) {
            canonicalUnit = metric.getMetricSource().getUnit();
        }
        return metric.getMetricName() + ": own " +
                metric.ownTimeValue().to(canonicalUnit) + ", total " + metric.totalValue().to(canonicalUnit);
    }


    public <T extends Quantity> void normalizeUnits(Unit<T> originalUnit, Unit<T> normalizedUnit) {
        normalizations.put(originalUnit, normalizedUnit);
    }
}
