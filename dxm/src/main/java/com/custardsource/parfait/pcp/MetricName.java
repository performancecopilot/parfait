package com.custardsource.parfait.pcp;

public final class MetricName {
    private final String metric;
    private final String instance;

    private MetricName(String metric, String instance) {
        if (metric == null) {
            throw new NullPointerException("Metric name may not be null");
        }
        this.metric = metric;
        this.instance = instance;
    }

    String getMetric() {
        return metric;
    }

    String getInstance() {
        return instance;
    }

    boolean hasInstance() {
        return instance == null;
    }

    public static MetricName withInstance(String metric, String instance) {
        return new MetricName(metric, instance);
    }

    public static MetricName withoutInstance(String metric) {
        return new MetricName(metric, null);
    }

    @Override
    public int hashCode() {
        final int prime = 191;
        int result = 1;
        result = prime * result + ((instance == null) ? 0 : instance.hashCode());
        result = prime * result + ((metric == null) ? 0 : metric.hashCode());
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

        return metric.equals(other.metric);
    }

    @Override
    public String toString() {
        return (instance == null) ? metric : metric + "[" + instance + "]";
    }
}
