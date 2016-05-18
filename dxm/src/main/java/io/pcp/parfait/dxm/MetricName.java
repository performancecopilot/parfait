package io.pcp.parfait.dxm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MetricName {
    private static final String IDENTIFIER_SECTION = "[\\p{Alnum}_/]+";
    private static final String VALID_METRIC_NAME = "\\A((_ID_)(\\._ID_)*)(\\[(_ID_)\\])?((\\._ID_)*)\\z"
            .replace("_ID_", IDENTIFIER_SECTION);
    private static final Pattern METRIC_PATTERN = Pattern.compile(VALID_METRIC_NAME);
    private static final int METRIC_PREFIX_INDEX = 1;
    private static final int METRIC_INSTANCE_INDEX = 5;
    private static final int METRIC_SUFFIX_INDEX = 6;

    private final String prefix;
    private final String suffix;
    private final String instance;

    private MetricName(String prefix, String suffix, String instance) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.instance = instance;
    }

    public String getMetric() {
        return prefix + suffix;
    }

    String getInstance() {
        return instance;
    }

    public String getInstanceDomainTag() {
        return prefix;
    }

    boolean hasInstance() {
        return instance != null;
    }

    public static MetricName parse(String metric) {
        Matcher m = METRIC_PATTERN.matcher(metric);
        if (!m.matches()) {
            throw new IllegalArgumentException(String.format("invalid metric name '%s'", metric));
        }

        return new MetricName(m.group(METRIC_PREFIX_INDEX), m.group(METRIC_SUFFIX_INDEX), m
                .group(METRIC_INSTANCE_INDEX));
    }

    @Override
    public int hashCode() {
        final int prime = 191;
        int result = 1;
        result = prime * result + ((instance == null) ? 0 : instance.hashCode());
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MetricName other = (MetricName) obj;
        if (instance == null) {
            if (other.instance != null) {
                return false;
            }
        } else if (!instance.equals(other.instance)) {
            return false;
        }

        return prefix.equals(other.prefix) && suffix.equals(other.suffix);
    }

    @Override
    public String toString() {
        return (instance == null) ? prefix + suffix : prefix + "[" + instance + "]" + suffix;
    }
}
