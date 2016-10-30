package io.pcp.parfait.dxm;

import static io.pcp.parfait.dxm.PcpMetricInfoV1.METRIC_NAME_LIMIT;
import static io.pcp.parfait.dxm.PcpMmvWriter.PCP_CHARSET;

class MmvV1MetricNameValidator implements MetricNameValidator {

    /**
     * The maximum length of an instance name able to be exported to the MMV agent. Note that this
     * is relative to PCP_CHARSET (it's a measure of the maximum number of bytes, not the
     * Java String length)
     */
    private static final int INSTANCE_NAME_LIMIT = 63;

    public void validateNameConstraints(MetricName metricName) {
        validateName(metricName);
        validateInstance(metricName);
    }

    private void validateInstance(MetricName metricName) {
        if (metricName.hasInstance()
                && metricName.getInstance().getBytes(PCP_CHARSET).length > INSTANCE_NAME_LIMIT) {
            throw new IllegalArgumentException("Cannot add metric " + metricName
                    + "; instance name is too long");
        }
    }

    private void validateName(MetricName metricName) {
        if (metricName.getMetric().getBytes(PCP_CHARSET).length > METRIC_NAME_LIMIT) {
            throw new IllegalArgumentException("Cannot add metric " + metricName
                    + "; name exceeds length limit");
        }
    }

}