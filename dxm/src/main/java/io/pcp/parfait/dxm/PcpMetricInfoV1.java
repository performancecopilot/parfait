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

import io.pcp.parfait.dxm.PcpMmvWriter.Store;
import io.pcp.parfait.dxm.semantics.UnitMapping;

import java.nio.ByteBuffer;
import java.util.Set;

import static io.pcp.parfait.dxm.PcpMmvWriter.PCP_CHARSET;

final class PcpMetricInfoV1 extends PcpMetricInfo {
    /**
     * The maximum length of a metric name able to be exported to the MMV agent. Note that this is
     * relative to {@link PcpMmvWriter#PCP_CHARSET} (it's a measure of the maximum number of bytes, not the Java
     * String length)
     */
    static final int METRIC_NAME_LIMIT = 63;


    private static final int METRIC_LENGTH = 104;

    private PcpMetricInfoV1(String metricName, int id) {
        super(metricName, id);
    }

    @Override
    public void writeToMmv(ByteBuffer byteBuffer) {
        byteBuffer.position(offset);

        int originalPosition = byteBuffer.position();

        byteBuffer.put(metricName.getBytes(PCP_CHARSET));
        byteBuffer.put((byte) 0);
        byteBuffer.position(originalPosition + METRIC_NAME_LIMIT + 1);
        byteBuffer.putInt(getId());
        byteBuffer.putInt(typeHandler.getMetricType().getIdentifier());
        byteBuffer.putInt(getSemantics().getPcpValue());
        byteBuffer.putInt(UnitMapping.getDimensions(getUnit(), metricName));
        if (domain != null) {
            byteBuffer.putInt(domain.getId());
        } else {
            byteBuffer.putInt(DEFAULT_INSTANCE_DOMAIN_ID);
        }
        // Just padding
        byteBuffer.putInt(0);
        byteBuffer.putLong(getStringOffset(shortHelpText));
        byteBuffer.putLong(getStringOffset(longHelpText));

    }

    @Override
    public int byteSize() {
        return METRIC_LENGTH;
    }

    static final class MetricInfoStoreV1 extends Store<PcpMetricInfo> {
        MetricInfoStoreV1(IdentifierSourceSet identifierSources) {
            super(identifierSources.metricSource());
        }

        @Override
        protected PcpMetricInfo newInstance(String name, Set<Integer> usedIds) {
            return new PcpMetricInfoV1(name, identifierSource.calculateId(name, usedIds));
        }
    }

}
