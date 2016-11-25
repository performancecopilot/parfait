package io.pcp.parfait.dxm;

import static io.pcp.parfait.dxm.PcpMmvWriter.PCP_CHARSET;

class MetricNameValidator {

    private final int nameLimit;
    private final int domainLimit;

    MetricNameValidator(int nameLimit, int domainLimit) {
        this.nameLimit = nameLimit;
        this.domainLimit = domainLimit;
    }

    void validateNameConstraints(MetricName metricName) {
        validateName(metricName);
        validateInstance(metricName);
    }

    private void validateInstance(MetricName metricName) {
        if (metricName.hasInstance()
                && metricName.getInstance().getBytes(PCP_CHARSET).length > domainLimit) {
            throw new IllegalArgumentException("Cannot add metric " + metricName
                    + "; instance name is too long");
        }
    }

    private void validateName(MetricName metricName) {
        if (metricName.getMetric().getBytes(PCP_CHARSET).length > nameLimit) {
            throw new IllegalArgumentException("Cannot add metric " + metricName
                    + "; name exceeds length limit");
        }
    }


}
