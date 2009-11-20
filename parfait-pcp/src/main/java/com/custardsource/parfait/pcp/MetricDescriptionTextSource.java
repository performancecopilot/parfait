package com.custardsource.parfait.pcp;

import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.dxm.MetricName;

public class MetricDescriptionTextSource implements TextSource {
    @Override
    public String getText(Monitorable<?> monitorable, MetricName mappedName) {
        return monitorable.getDescription();
    }
}
