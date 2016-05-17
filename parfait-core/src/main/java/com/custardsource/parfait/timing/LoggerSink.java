package com.custardsource.parfait.timing;

import java.util.Collection;
import java.util.Map;

import javax.measure.Unit;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    String buildSingleMetricResult(MetricMeasurement metric) {
    	Unit canonicalUnit = normalizations.get(metric.getMetricSource().getUnit());
        if (canonicalUnit == null) {
            canonicalUnit = metric.getMetricSource().getUnit();
        }
        return metric.getMetricName() + ": own " +
                metric.ownTimeValue().to(canonicalUnit) + ", total " + metric.totalValue().to(canonicalUnit);
    }


    public void normalizeUnits(Unit<?> originalUnit, Unit<?> normalizedUnit) {
        normalizations.put(originalUnit, normalizedUnit);
    }
}
