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
