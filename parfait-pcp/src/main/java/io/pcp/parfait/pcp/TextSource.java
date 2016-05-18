package io.pcp.parfait.pcp;

import io.pcp.parfait.Monitorable;
import io.pcp.parfait.dxm.MetricName;

public interface TextSource {
    public String getText(Monitorable<?> monitorable, MetricName mappedName);
}
