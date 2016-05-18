package io.pcp.parfait.pcp;

import io.pcp.parfait.Monitorable;
import io.pcp.parfait.dxm.MetricName;

public class MetricDescriptionTextSource implements TextSource {
    @Override
    public String getText(Monitorable<?> monitorable, MetricName mappedName) {
        return monitorable.getDescription();
    }
}
